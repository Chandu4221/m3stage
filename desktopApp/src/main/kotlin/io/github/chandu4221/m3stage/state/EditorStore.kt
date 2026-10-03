package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findScreenContaining
import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditorStore(
    private val idGenerator: IdGenerator,
    private val repository: ProjectRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // --- Compose State (UI-reactive) ---
    private val _selectedNodeId = mutableStateOf<NodeId?>(null)
    val selectedNodeId: State<NodeId?> = _selectedNodeId

    private val _activeScreenId = mutableStateOf<ScreenId?>(null)
    val activeScreenId: State<ScreenId?> = _activeScreenId

    // --- StateFlow (Observable state) ---
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _undoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    val undoStack: StateFlow<List<EditorCommand>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    val redoStack: StateFlow<List<EditorCommand>> = _redoStack.asStateFlow()

    // --- SharedFlow (One-shot events) ---
    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    // --- Actions ---

    fun selectNode(nodeId: NodeId?) {
        _selectedNodeId.value = nodeId

        val currentProject = _project.value
        if (nodeId != null && currentProject != null) {
            val screen = currentProject.findScreenContaining(nodeId)
            if (screen != null && _activeScreenId.value != screen.id) {
                _activeScreenId.value = screen.id
            }
        }
    }

    fun setActiveScreen(screenId: ScreenId) {
        _activeScreenId.value = screenId
        _selectedNodeId.value = null
    }

    fun execute(command: EditorCommand) {
        val currentProject = _project.value ?: return
        val newProject = command.execute(currentProject)

        val errors = ProjectValidation.validate(newProject)
        if (errors.isNotEmpty()) {
            scope.launch { _events.emit(EditorEvent.ShowError("Invalid state: ${errors.first()}")) }
            return
        }

        _project.value = newProject
        _undoStack.value = _undoStack.value + command
        _redoStack.value = emptyList()
    }

    fun undo() {
        val command = _undoStack.value.lastOrNull() ?: return
        val currentProject = _project.value ?: return

        val revertedProject = command.undo(currentProject)
        _project.value = revertedProject
        _undoStack.value = _undoStack.value.dropLast(1)
        _redoStack.value = _redoStack.value + command
    }

    fun redo() {
        val command = _redoStack.value.lastOrNull() ?: return
        val currentProject = _project.value ?: return

        val reappliedProject = command.execute(currentProject)
        _project.value = reappliedProject
        _redoStack.value = _redoStack.value.dropLast(1)
        _undoStack.value = _undoStack.value + command
    }

    fun loadProject() {
        scope.launch {
            try {
                val loaded = repository.load()
                if (loaded != null) {
                    val errors = ProjectValidation.validate(loaded)
                    if (errors.isNotEmpty()) {
                        _events.emit(EditorEvent.ShowError("Corrupt project: ${errors.first()}"))
                        return@launch
                    }
                    _project.value = loaded
                    _activeScreenId.value = loaded.screens.firstOrNull()?.id
                    _events.emit(EditorEvent.LoadSuccess)
                }
            } catch (e: Exception) {
                _events.emit(EditorEvent.ShowError("Failed to load: ${e.message}"))
            }
        }
    }

    fun saveProject() {
        val currentProject = _project.value ?: return
        scope.launch {
            try {
                repository.save(currentProject)
                _events.emit(EditorEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(EditorEvent.ShowError("Failed to save: ${e.message}"))
            }
        }
    }

    /**
     * Builds a fully-formed node tree with new IDs and default props,
     * then executes the AddNodeCommand.
     */
    fun addNodeToActiveScreen(parentId: NodeId, componentType: ComponentType) {
        val screenId = _activeScreenId.value ?: return
        val definition = ComponentCatalog.getByType(componentType) ?: return

        // Recursive builder to create the node and all its default children with fresh IDs
        fun buildNode(type: ComponentType): DesignNode {
            val def = ComponentCatalog.getByType(type) ?: throw IllegalArgumentException("Unknown type: $type")
            return DesignNode(
                id = idGenerator.nextNodeId(),
                type = def.type,
                props = def.defaultProps,
                children = def.defaultChildTypes.map { buildNode(it) }
            )
        }

        val newNode = buildNode(componentType)
        execute(AddNodeCommand(screenId, parentId, newNode))
    }
}