package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EditorStore(
    idGenerator: IdGenerator,
    repository: ProjectRepository
) : SelectionState, HistoryState, ProjectSession {

    private val context = EditorContext(idGenerator, repository)
    private val selectionDelegate = SelectionDelegate()
    private val historyDelegate = HistoryDelegate(context)
    private val sessionDelegate = ProjectSessionDelegate(context)

    // Scope for observing state changes
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // Auto-select the first screen whenever a project is loaded or created
        storeScope.launch {
            project.collect { currentProject ->
                if (currentProject != null && activeScreenId.value == null) {
                    currentProject.screens.firstOrNull()?.let { firstScreen ->
                        setActiveScreen(firstScreen.id)
                    }
                }
            }
        }
    }

    // --- SelectionState ---
    override val selectedNodeId = selectionDelegate.selectedNodeId
    override val activeScreenId = selectionDelegate.activeScreenId
    override val lockedNodeIds = selectionDelegate.lockedNodeIds
    override fun selectNode(id: NodeId?) = selectionDelegate.selectNode(id)
    override fun setActiveScreen(id: ScreenId) = selectionDelegate.setActiveScreen(id)
    override fun toggleLock(id: NodeId) = selectionDelegate.toggleLock(id)
    override fun isNodeLocked(id: NodeId) = selectionDelegate.isNodeLocked(id)

    // --- HistoryState ---
    override val undoStack = historyDelegate.undoStack
    override val redoStack = historyDelegate.redoStack
    override fun execute(command: EditorCommand) = historyDelegate.execute(command)
    override fun undo() = historyDelegate.undo()
    override fun redo() = historyDelegate.redo()

    // --- ProjectSession ---
    override val project = sessionDelegate.project
    override val events = sessionDelegate.events
    override fun loadProject() = sessionDelegate.loadProject()
    override fun saveProject() = sessionDelegate.saveProject()
    override fun createNewProject() = sessionDelegate.createNewProject()

    // --- Bridge: Add Node ---
    fun addNodeToActiveScreen(parentId: NodeId, componentType: ComponentType) {
        val screenId = activeScreenId.value ?: return

        fun buildNode(type: ComponentType): DesignNode {
            val def = ComponentCatalog.getByType(type) ?: throw IllegalArgumentException("Unknown type: $type")
            return DesignNode(
                id = context.idGenerator.nextNodeId(),
                type = def.type,
                props = def.defaultProps,
                children = def.defaultChildTypes.map { buildNode(it) }
            )
        }

        val newNode = buildNode(componentType)
        execute(AddNodeCommand(screenId, parentId, newNode))
    }

    // --- Bridge: Toggle Visibility ---
    fun toggleVisibility(nodeId: NodeId) {
        val screenId = activeScreenId.value ?: return
        val currentProject = project.value ?: return
        val screen = currentProject.findScreen(screenId) ?: return
        val node = screen.root.findNode(nodeId) ?: return

        execute(
            UpdateVisibilityCommand(
                screenId = screenId,
                nodeId = nodeId,
                oldVisibility = node.isVisible,
                newVisibility = !node.isVisible
            )
        )
    }
}