package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectSessionDelegate(private val context: EditorContext) : ProjectSession {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val project: StateFlow<Project?> = context.projectFlow.asStateFlow()
    override val events: SharedFlow<EditorEvent> = context.eventFlow.asSharedFlow()

    override fun loadProject() {
        scope.launch {
            try {
                val loaded = context.repository.load()
                if (loaded != null && ProjectValidation.validate(loaded).isEmpty()) {
                    context.projectFlow.value = loaded
                    context.eventFlow.emit(EditorEvent.LoadSuccess)
                } else if (loaded == null) {
                    createNewProject()
                }
            } catch (e: Exception) {
                context.eventFlow.emit(EditorEvent.LoadFailed)
            }
        }
    }

    override fun saveProject() {
        val current = context.projectFlow.value ?: return
        scope.launch {
            try {
                context.repository.save(current)
                context.eventFlow.emit(EditorEvent.SaveSuccess)
            } catch (e: Exception) {
                context.eventFlow.emit(EditorEvent.SaveFailed)
            }
        }
    }

    override fun createNewProject() {
        val idGen = context.idGenerator
        val rootKind = ComponentKind.Column
        val rootDef = ComponentCatalog[rootKind]

        val newProject = Project(
            id = idGen.nextProjectId(),
            name = "Untitled",
            basePackage = "com.example.app",
            screens = listOf(
                Screen(
                    id = idGen.nextScreenId(),
                    name = "Home",
                    route = "/",
                    root = DesignNode(
                        id = idGen.nextNodeId(),
                        kind = rootKind,
                        props = rootDef.createDefaultProps(),
                        children = emptyList()
                    )
                )
            )
        )
        context.projectFlow.value = newProject
    }

    override fun exportCode() {
        val current = context.projectFlow.value ?: return
        scope.launch {
            try {
                // 1. Generate the code using the domain port
                val generatedProject = context.codeGenerator.generate(current)

                // 2. Define output directory (MVP: User home directory)
                val outputDir = java.io.File(System.getProperty("user.home"), "m3stage-export")
                if (!outputDir.exists()) outputDir.mkdirs()

                // 3. Write files to disk
                generatedProject.files.forEach { file ->
                    java.io.File(outputDir, file.fileName).writeText(file.content)
                }

                context.eventFlow.emit(EditorEvent.ExportSuccess(outputDir.absolutePath))
            } catch (e: Exception) {
                context.eventFlow.emit(EditorEvent.ExportFailed(e.message ?: "Unknown error"))
            }
        }
    }
}