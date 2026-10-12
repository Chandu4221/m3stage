package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findScreen
import kotlinx.coroutines.*

class EditorStore(
    idGenerator: IdGenerator,
    repository: ProjectRepository,
    codeGenerator: CodeGenerator,
) : SelectionState, HistoryState, ProjectSession, AutoCloseable {

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
    override fun exportCode(customPackageName: String?) = sessionDelegate.exportCode(customPackageName)

    fun notifyUser(message: String) {
        context.eventFlow.tryEmit(EditorEvent.ShowError(message))
    }

    // --- Bridge: Add Node ---
    fun addNodeToActiveScreen(parentId: NodeId, kind: ComponentKind) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot add node: no active screen selected.")
            return
        }
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot add node: no project open.")
            return
        }
        val activeScreen = currentProject.findScreen(screenId)
        if (activeScreen == null) {
            notifyUser("Cannot add node: active screen not found.")
            return
        }
        val targetNode = activeScreen.root.findNode(parentId)
        if (targetNode == null) {
            notifyUser("Cannot add node: target parent not found in active screen.")
            return
        }

        var targetParentId = parentId
        var targetSlotId: io.github.chandu4221.m3stage.component.slot.SlotId? = null

        if (targetNode.kind == ComponentKind.Scaffold) {
            when (kind) {
                ComponentKind.TopAppBar -> {
                    targetSlotId = io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition.TopBarSlot.id
                }
                ComponentKind.NavigationBar -> {
                    targetSlotId = io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition.BottomBarSlot.id
                }
                ComponentKind.FloatingActionButton -> {
                    targetSlotId = io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition.FloatingActionButtonSlot.id
                }
                else -> {
                    // Route into the Scaffold's content column if available, else into ContentSlot
                    val contentColumn = targetNode.allDirectChildren.firstOrNull { it.kind == ComponentKind.Column }
                    if (contentColumn != null) {
                        targetParentId = contentColumn.id
                        targetSlotId = null
                    } else {
                        targetSlotId = io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition.ContentSlot.id
                    }
                }
            }
        }

        val newNode = ComponentCatalog[kind].createDefaultSubtree(context.idGenerator)
        execute(AddNodeCommand(screenId, targetParentId, newNode, targetSlotId))
    }

    // --- Bridge: Toggle Visibility ---
    fun toggleVisibility(nodeId: NodeId) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot toggle visibility: no active screen selected.")
            return
        }
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot toggle visibility: no project open.")
            return
        }
        val screen = currentProject.findScreen(screenId)
        if (screen == null) {
            notifyUser("Cannot toggle visibility: active screen not found.")
            return
        }
        val node = screen.root.findNode(nodeId)
        if (node == null) {
            notifyUser("Cannot toggle visibility: node not found.")
            return
        }

        execute(
            UpdateVisibilityCommand(
                screenId = screenId,
                nodeId = nodeId,
                oldVisibility = node.isVisible,
                newVisibility = !node.isVisible
            )
        )
    }

    // --- Bridge: Remove Node ---
    fun removeNode(nodeId: NodeId) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot remove node: no active screen selected.")
            return
        }
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot remove node: no project open.")
            return
        }
        val screen = currentProject.findScreen(screenId)
        if (screen == null) {
            notifyUser("Cannot remove node: active screen not found.")
            return
        }

        // Prevent deleting the screen's root node
        if (screen.root.id == nodeId) {
            notifyUser("Cannot delete root node '${screen.root.kind.displayName}'.")
            return
        }

        execute(RemoveNodeCommand(screenId, nodeId))
        if (selectedNodeId.value == nodeId) {
            selectNode(null)
        }
    }

    // --- Bridge: Add New Screen ---
    fun addNewScreen(name: String, route: String, device: DevicePreset? = null) {
        val idGen = context.idGenerator
        val rootNode = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val screenId = idGen.nextScreenId()
        val newScreen = Screen(
            id = screenId,
            name = name,
            route = route,
            device = device,
            root = rootNode
        )
        execute(AddScreenCommand(newScreen))
        setActiveScreen(screenId)
    }

    // --- Bridge: Update Project Theme ---
    fun updateSeedColor(newSeed: Long) {
        val current = project.value
        if (current == null) {
            notifyUser("Cannot update theme: no project open.")
            return
        }
        execute(
            UpdateThemeCommand(
                oldSeedColor = current.seedColor,
                oldIsDarkMode = current.isDarkMode,
                newSeedColor = newSeed,
                newIsDarkMode = current.isDarkMode
            )
        )
    }

    fun toggleDarkMode() {
        val current = project.value
        if (current == null) {
            notifyUser("Cannot toggle dark mode: no project open.")
            return
        }
        execute(
            UpdateThemeCommand(
                oldSeedColor = current.seedColor,
                oldIsDarkMode = current.isDarkMode,
                newSeedColor = current.seedColor,
                newIsDarkMode = !current.isDarkMode
            )
        )
    }

    // --- Bridge: Modifier Operations ---
    fun addModifier(nodeId: NodeId, modifier: ModifierSpec) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot add modifier: no active screen selected.")
            return
        }
        execute(AddModifierCommand(screenId, nodeId, modifier))
    }

    fun removeModifier(nodeId: NodeId, index: Int) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot remove modifier: no active screen selected.")
            return
        }
        execute(RemoveModifierCommand(screenId, nodeId, index))
    }

    fun updateModifier(nodeId: NodeId, index: Int, newModifier: ModifierSpec) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot update modifier: no active screen selected.")
            return
        }
        execute(
            UpdateModifierCommand(
                screenId = screenId,
                nodeId = nodeId,
                index = index,
                newModifier = newModifier
            )
        )
    }

    override fun close() {
        storeScope.cancel()
    }
}
