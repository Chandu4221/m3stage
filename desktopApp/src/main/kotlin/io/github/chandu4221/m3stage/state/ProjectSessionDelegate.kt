package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class ProjectSessionDelegate(
    private val context: EditorContext,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : ProjectSession, AutoCloseable {

    private val saveMutex = Mutex()

    var autosaveBlocked = false
        private set

    private val _isDirty = MutableStateFlow(false)
    override val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    override val project: StateFlow<Project?> = context.projectFlow.asStateFlow()
    override val events: SharedFlow<EditorEvent> = context.eventFlow.asSharedFlow()

    private var lastSavedOrLoadedProject: Project? = null

    init {
        // Debounced autosave observer
        scope.launch {
            var isInitial = true
            context.projectFlow
                .collect { p ->
                    if (isInitial) {
                        isInitial = false
                    } else if (p != null && p != lastSavedOrLoadedProject) {
                        _isDirty.value = true
                    }
                }
        }

        scope.launch {
            // Drop initial null/fresh emission from triggering autosave immediately
            context.projectFlow
                .debounce(800.milliseconds)
                .collect { proj ->
                    saveMutex.withLock {
                        if (proj != null && _isDirty.value && !autosaveBlocked) {
                            try {
                                context.repository.save(proj)
                                lastSavedOrLoadedProject = proj
                                _isDirty.value = false
                                context.eventFlow.emit(EditorEvent.SaveSuccess)
                            } catch (e: Exception) {
                                context.eventFlow.emit(EditorEvent.SaveFailed)
                            }
                        }
                    }
                }
        }
    }

    private var currentLoadId = 0L

    override fun loadProject() {
        val loadId = ++currentLoadId
        val initialProjectState = context.projectFlow.value
        scope.launch {
            try {
                val loaded = context.repository.load()
                if (loadId != currentLoadId) {
                    return@launch
                }
                if (context.projectFlow.value != initialProjectState) {
                    context.eventFlow.emit(
                        EditorEvent.LoadFailed("Load aborted: project was modified while loading was in progress.")
                    )
                    return@launch
                }
                if (loaded != null) {
                    val validationErrors = ProjectValidation.validate(loaded)
                    if (validationErrors.isEmpty()) {
                        autosaveBlocked = false
                        lastSavedOrLoadedProject = loaded
                        context.projectFlow.value = loaded
                        _isDirty.value = false
                        context.eventFlow.emit(EditorEvent.LoadSuccess)
                    } else {
                        autosaveBlocked = true
                        _isDirty.value = false
                        context.eventFlow.emit(
                            EditorEvent.LoadFailed(
                                "Invalid project: ${validationErrors.joinToString("; ")}. Initialized fresh project."
                            )
                        )
                        createNewProject()
                    }
                } else {
                    // Truly first launch: file does not exist on disk
                    autosaveBlocked = false
                    createNewProject()
                }
            } catch (e: Exception) {
                if (loadId != currentLoadId || context.projectFlow.value != initialProjectState) {
                    return@launch
                }
                autosaveBlocked = true
                _isDirty.value = false
                context.eventFlow.emit(
                    EditorEvent.LoadFailed("${e.message ?: "Failed to load project"}. Initialized fresh project.")
                )
                createNewProject()
            }
        }
    }

    override fun saveProject() {
        val current = context.projectFlow.value ?: return
        scope.launch {
            saveMutex.withLock {
                try {
                    context.repository.save(current)
                    lastSavedOrLoadedProject = current
                    autosaveBlocked = false
                    _isDirty.value = false
                    context.eventFlow.emit(EditorEvent.SaveSuccess)
                } catch (e: Exception) {
                    context.eventFlow.emit(EditorEvent.SaveFailed)
                }
            }
        }
    }

    override fun createNewProject() {
        currentLoadId++
        val idGen = context.idGenerator
        val rootNode = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)

        val newProject = Project(
            id = idGen.nextProjectId(),
            name = "Untitled",
            basePackage = "com.example.app",
            screens = listOf(
                Screen(
                    id = idGen.nextScreenId(),
                    name = "Home",
                    route = "/",
                    root = rootNode
                )
            )
        )
        val errors = io.github.chandu4221.m3stage.validation.ProjectValidation.validate(newProject)
        if (errors.isNotEmpty()) {
            throw IllegalStateException("Factory produced invalid project: ${errors.joinToString("; ")}")
        }

        context.projectFlow.value = newProject
    }

    fun flushSave() {
        val current = context.projectFlow.value ?: return
        if (_isDirty.value && !autosaveBlocked) {
            runBlocking {
                try {
                    withTimeout(2000.milliseconds) {
                        saveMutex.withLock {
                            if (_isDirty.value && !autosaveBlocked) {
                                context.repository.save(current)
                                lastSavedOrLoadedProject = current
                                _isDirty.value = false
                                context.eventFlow.emit(EditorEvent.SaveSuccess)
                            }
                        }
                    }
                } catch (e: Exception) {
                    context.eventFlow.emit(EditorEvent.SaveFailed)
                }
            }
        }
    }

    override fun close() {
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