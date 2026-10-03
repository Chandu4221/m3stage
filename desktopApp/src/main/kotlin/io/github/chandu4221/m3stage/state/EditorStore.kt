package io.github.chandu4221.m3stage.state


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.state.commands.AddNodeCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central state holder for the editor.
 *
 * Uses Compose State for UI-reactive fields (selection, hover)
 * and StateFlow for cross-module observable state (project, history).
 * SharedFlow for one-shot events (toasts, errors).
 */
class EditorStore(
    private val idGenerator: IdGenerator,
    private val repository: ProjectRepository,
    private val createNodeUseCase: CreateNodeUseCase
) {
    // Scope for async operations (save/load)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // --- Compose State (UI-reactive, triggers recomposition) ---
    private val _selectedNodeId = mutableStateOf<NodeId?>(null)
    val selectedNodeId: State<NodeId?> = _selectedNodeId

    private val _activeScreenId = mutableStateOf<ScreenId?>(null)
    val activeScreenId: State<ScreenId?> = _activeScreenId

    private val _hoveredNodeId = mutableStateOf<NodeId?>(null)
    val hoveredNodeId: State<NodeId?> = _hoveredNodeId

    // --- StateFlow (observable across modules, always has a value) ---
    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _undoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    val undoStack: StateFlow<List<EditorCommand>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    val redoStack: StateFlow<List<EditorCommand>> = _redoStack.asStateFlow()

    // --- SharedFlow (one-shot events, no replay) ---
    private val _events = MutableSharedFlow<EditorEvent>()
    val events: SharedFlow<EditorEvent> = _events.asSharedFlow()

    // --- Commands ---

    fun selectNode(nodeId: NodeId?) {
        _selectedNodeId.value = nodeId
    }

    fun hoverNode(nodeId: NodeId?) {
        _hoveredNodeId.value = nodeId
    }

    fun setActiveScreen(screenId: ScreenId) {
        _activeScreenId.value = screenId
    }

    /**
     * Execute a command, apply it, and push to undo stack.
     * Clears the redo stack (standard undo/redo behavior).
     */
    fun execute(command: EditorCommand) {
        val currentProject = _project.value ?: return
        val newProject = command.execute(currentProject, idGenerator)

        _project.value = newProject
        _undoStack.value = _undoStack.value + command
        _redoStack.value = emptyList() // Clear redo on new action
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

        val reappliedProject = command.execute(currentProject, idGenerator)
        _project.value = reappliedProject
        _redoStack.value = _redoStack.value.dropLast(1)
        _undoStack.value = _undoStack.value + command
    }

    // --- Async operations ---

    fun loadProject() {
        scope.launch {
            try {
                val loaded = repository.load()
                _project.value = loaded
                loaded?.screens?.firstOrNull()?.let { firstScreen ->
                    _activeScreenId.value = firstScreen.id
                }
                _events.emit(EditorEvent.ShowToast("Project loaded"))
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
                _events.emit(EditorEvent.SaveFailed)
            }
        }
    }

    // --- Convenience: add node from palette ---

    fun addNodeToActiveScreen(parentId: NodeId, componentType: io.github.chandu4221.domain.model.ComponentType) {
        val screenId = _activeScreenId.value ?: return
        val newNode = createNodeUseCase.execute(componentType)
        execute(AddNodeCommand(screenId, parentId, newNode))
    }
}