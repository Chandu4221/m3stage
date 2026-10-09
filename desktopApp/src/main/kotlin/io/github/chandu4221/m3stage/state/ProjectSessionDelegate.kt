package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ProjectSessionDelegate(private val context: EditorContext) : ProjectSession {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val project: StateFlow<Project?> = context.projectFlow.asStateFlow()
    override val events: SharedFlow<EditorEvent> = context.eventFlow.asSharedFlow()

    override fun loadProject() {
        scope.launch {
            try {
                val loaded = context.repository.load()
                if (loaded != null) {
                    val validationErrors = ProjectValidation.validate(loaded)
                    if (validationErrors.isEmpty()) {
                        context.projectFlow.value = loaded
                        context.eventFlow.emit(EditorEvent.LoadSuccess)
                    } else {
                        context.eventFlow.emit(
                            EditorEvent.LoadFailed(
                                "Invalid project invariants: ${
                                    validationErrors.joinToString(
                                        "; "
                                    )
                                }"
                            )
                        )
                    }
                } else {
                    // Truly first launch: file does not exist on disk
                    createNewProject()
                }
            } catch (e: Exception) {
                context.eventFlow.emit(EditorEvent.LoadFailed(e.message ?: "Failed to load project"))
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
        val scaffoldDef = ComponentCatalog[ComponentKind.Scaffold]
        val topBarDef = ComponentCatalog[ComponentKind.TopAppBar]
        val columnDef = ComponentCatalog[ComponentKind.Column]

        val topBarNode = DesignNode(
            id = idGen.nextNodeId(),
            kind = ComponentKind.TopAppBar,
            props = topBarDef.createDefaultProps(),
            children = emptyList()
        )
        val contentColumnNode = DesignNode(
            id = idGen.nextNodeId(),
            kind = ComponentKind.Column,
            props = columnDef.createDefaultProps(),
            children = emptyList()
        )

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
                        kind = ComponentKind.Scaffold,
                        props = scaffoldDef.createDefaultProps(),
                        children = listOf(topBarNode, contentColumnNode)
                    )
                )
            )
        )
        context.projectFlow.value = newProject
    }

    fun close() {
        scope.cancel()
    }

    override fun exportCode(customPackageName: String?) {
        val current = context.projectFlow.value ?: return
        val effectiveProject = if (!customPackageName.isNullOrBlank()) {
            current.copy(basePackage = customPackageName.trim())
        } else {
            current
        }

        scope.launch {
            try {
                // 1. Offload CPU-heavy code generation to Dispatchers.Default
                val generatedProject = withContext(Dispatchers.Default) {
                    context.codeGenerator.generate(effectiveProject)
                }

                // 2. Offload ZIP packaging and disk I/O to Dispatchers.IO
                val zipFile = withContext(Dispatchers.IO) {
                    val downloadsDir = java.io.File(System.getProperty("user.home"), "Downloads")
                    val targetDir = if (downloadsDir.exists()) downloadsDir else java.io.File(System.getProperty("user.home"))
                    val zip = java.io.File(targetDir, "m3stage-export.zip")

                    java.util.zip.ZipOutputStream(java.io.FileOutputStream(zip)).use { zos ->
                        generatedProject.files.forEach { file ->
                            val packagePath = file.packageName.replace('.', '/')
                            val entryPath = if (packagePath.isEmpty()) file.fileName else "$packagePath/${file.fileName}"
                            zos.putNextEntry(java.util.zip.ZipEntry(entryPath))
                            zos.write(file.content.toByteArray(Charsets.UTF_8))
                            zos.closeEntry()
                        }
                    }
                    zip
                }

                // 3. Emit UI success event
                context.eventFlow.emit(EditorEvent.ExportSuccess(zipFile.absolutePath))
            } catch (e: Exception) {
                context.eventFlow.emit(EditorEvent.ExportFailed(e.message ?: "Unknown error"))
            }
        }
    }
}