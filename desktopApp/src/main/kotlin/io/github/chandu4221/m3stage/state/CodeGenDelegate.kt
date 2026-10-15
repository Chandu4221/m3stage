package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.adapter.codegen.util.KotlinIdentifierSanitizer
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.port.CodeGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the live code generator preview panel.
 */
sealed interface CodeGenUiState {
    data object Idle : CodeGenUiState
    data class Generating(val content: String? = null, val fileName: String? = null) : CodeGenUiState
    data class Ready(val fileName: String, val content: String) : CodeGenUiState
    data class Error(val message: String) : CodeGenUiState
}

/**
 * Manages debounced, cancelable code generation with monotonic token protection against stale results.
 */
class CodeGenDelegate(
    private val codeGenerator: CodeGenerator,
    private val scope: CoroutineScope,
    private val debounceMs: Long = 500L,
    private val computationDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AutoCloseable {

    private val _previewState = MutableStateFlow<CodeGenUiState>(CodeGenUiState.Idle)
    val previewState: StateFlow<CodeGenUiState> = _previewState.asStateFlow()

    private var latestToken = 0L
    private var activeJob: Job? = null

    /**
     * Requests a new code generation for [screen] within [project].
     * Subsequent requests before [debounceMs] cancel or supersede prior generations.
     */
    fun request(screen: Screen, project: Project) {
        val token = ++latestToken
        val (prevContent, prevFileName) = when (val s = _previewState.value) {
            is CodeGenUiState.Ready -> s.content to s.fileName
            is CodeGenUiState.Generating -> s.content to s.fileName
            else -> null to null
        }
        val targetFileName = "${KotlinIdentifierSanitizer.toPascalCase(screen.name)}.kt"
        _previewState.value = CodeGenUiState.Generating(
            content = prevContent,
            fileName = prevFileName ?: targetFileName
        )

        activeJob?.cancel()
        activeJob = scope.launch {
            delay(debounceMs)
            if (token != latestToken) return@launch

            try {
                val generatedProject = withContext(computationDispatcher) {
                    codeGenerator.generate(project)
                }
                if (token != latestToken) return@launch

                val pascalName = KotlinIdentifierSanitizer.toPascalCase(screen.name)
                val matchingFile = generatedProject.files.firstOrNull { it.fileName.startsWith(pascalName) }
                    ?: generatedProject.files.firstOrNull()

                val fileName = matchingFile?.fileName ?: targetFileName
                val content = matchingFile?.content ?: ""

                _previewState.value = CodeGenUiState.Ready(fileName, content)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (token == latestToken) {
                    _previewState.value = CodeGenUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Cancels any in-flight code generation and resets state to Idle.
     */
    override fun close() {
        latestToken++
        activeJob?.cancel()
        activeJob = null
        _previewState.value = CodeGenUiState.Idle
    }
}
