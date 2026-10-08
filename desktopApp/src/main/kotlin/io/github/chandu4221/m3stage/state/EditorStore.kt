package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
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
    repository: ProjectRepository,
    codeGenerator: CodeGenerator,
) : SelectionState, HistoryState, ProjectSession {

    private val context = EditorContext(idGenerator, repository, codeGenerator)
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

    override fun exportCode() = sessionDelegate.exportCode()

    // --- Bridge: Add Node ---
    fun addNodeToActiveScreen(parentId: NodeId, kind: ComponentKind) {
        val screenId = activeScreenId.value ?: return

        fun buildNode(targetKind: ComponentKind): DesignNode {
            val def = ComponentCatalog[targetKind]
            // Atomic Design default anatomy: When dropping a Button, pre-populate with a Text atom!
            val defaultChildren = when (targetKind) {
                ComponentKind.Button -> listOf(buildNode(ComponentKind.Text))
                else -> emptyList()
            }

            return DesignNode(
                id = context.idGenerator.nextNodeId(),
                kind = def.kind,
                props = def.createDefaultProps(),
                modifiers = emptyList(),
                children = defaultChildren,
                isVisible = true
            )
        }

        val newNode = buildNode(kind)
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


    // --- Bridge: Add New Screen ---
    fun addNewScreen(name: String, route: String, device: DevicePreset? = null) {
        val rootKind = ComponentKind.Column
        val rootDef = ComponentCatalog[rootKind]
        val screenId = context.idGenerator.nextScreenId()
        val newScreen = Screen(
            id = screenId,
            name = name,
            route = route,
            device = device,
            root = DesignNode(
                id = context.idGenerator.nextNodeId(),
                kind = rootKind,
                props = rootDef.createDefaultProps(),
                children = emptyList()
            )
        )
        execute(AddScreenCommand(newScreen))
        setActiveScreen(screenId)
    }

}