package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findAcceptingSlot
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findScreen
import kotlinx.coroutines.*

class EditorStore(
    idGenerator: IdGenerator,
    repository: ProjectRepository,
    codeGenerator: CodeGenerator,
) : SelectionState, HistoryState, ProjectSession, AutoCloseable {

    private val context = EditorContext(idGenerator, repository, codeGenerator)
    val codeGenerator: CodeGenerator get() = context.codeGenerator
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
    override val isDirty = sessionDelegate.isDirty
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

        val parentDef = ComponentCatalog.find(targetNode.kind)
        if (parentDef == null) {
            notifyUser("Cannot add node: unrecognized component kind '${targetNode.kind}'.")
            return
        }

        var targetParentId = parentId
        var targetSlotId: io.github.chandu4221.m3stage.component.slot.SlotId? = null

        val acceptingSlotId = parentDef.findAcceptingSlot(kind)
        if (acceptingSlotId != null) {
            val slotDef = parentDef.findSlot(acceptingSlotId)
            val existingInSlot = targetNode.slots[acceptingSlotId] ?: emptyList()
            if (slotDef != null && slotDef.isFull(existingInSlot.size)) {
                val occupant = existingInSlot.firstOrNull()?.kind?.displayName ?: "another component"
                notifyUser("${targetNode.kind.displayName}'s ${acceptingSlotId.value} slot is already occupied by a $occupant.")
                return
            }
            targetSlotId = acceptingSlotId
        } else if (parentDef.canAcceptChild(kind)) {
            targetParentId = parentId
            targetSlotId = null
        } else {
            val containerChild = io.github.chandu4221.m3stage.query.findDefaultContainerChild(targetNode, kind)
            if (containerChild != null) {
                targetParentId = containerChild.id
                val childDef = ComponentCatalog.find(containerChild.kind)
                targetSlotId = childDef?.findAcceptingSlot(kind)
            } else {
                val tip = if (targetNode.kind == ComponentKind.Scaffold) {
                    "Scaffold content accepts containers — try adding a Column first, or drop the ${kind.displayName} onto a Column."
                } else {
                    "Try adding a container first, or select a compatible container."
                }
                notifyUser("${kind.displayName} can't be added to ${targetNode.kind.displayName}. $tip")
                return
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

    // --- Bridge: Duplicate Node ---
    fun duplicateNode(nodeId: NodeId) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot duplicate node: no active screen selected.")
            return
        }
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot duplicate node: no project open.")
            return
        }
        val screen = currentProject.findScreen(screenId)
        if (screen == null) {
            notifyUser("Cannot duplicate node: active screen not found.")
            return
        }

        // Prevent duplicating the screen's root node
        if (screen.root.id == nodeId) {
            notifyUser("Cannot duplicate root node '${screen.root.kind.displayName}'.")
            return
        }

        val originalNode = screen.root.findNode(nodeId)
        if (originalNode == null) {
            notifyUser("Cannot duplicate node: node not found.")
            return
        }

        fun reIdSubtree(node: DesignNode): DesignNode {
            val freshId = context.idGenerator.nextNodeId()
            val freshChildren = node.children.map { reIdSubtree(it) }
            val freshSlots = node.slots.mapValues { (_, slotChildren) -> slotChildren.map { reIdSubtree(it) } }
            return node.copy(id = freshId, children = freshChildren, slots = freshSlots)
        }

        val freshSubtree = reIdSubtree(originalNode)
        execute(DuplicateNodeCommand(screenId, nodeId, freshSubtree))
        selectNode(freshSubtree.id)
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

    // --- Bridge: Rename Screen ---
    fun renameScreen(screenId: ScreenId, newName: String) {
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot rename screen: no project open.")
            return
        }
        val screen = currentProject.findScreen(screenId)
        if (screen == null) {
            notifyUser("Cannot rename screen: screen not found.")
            return
        }
        val sanitizedName = newName.trim()
        if (!sanitizedName.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            notifyUser("Invalid screen name '$sanitizedName'. Must be a valid identifier (alphanumeric and underscores, starting with letter).")
            return
        }
        if (currentProject.screens.any { it.id != screenId && it.name.equals(sanitizedName, ignoreCase = true) }) {
            notifyUser("A screen named '$sanitizedName' already exists.")
            return
        }
        execute(RenameScreenCommand(screenId, screen.name, sanitizedName))
    }

    // --- Bridge: Delete Screen ---
    fun deleteScreen(screenId: ScreenId) {
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot delete screen: no project open.")
            return
        }
        if (currentProject.screens.size <= 1) {
            notifyUser("Cannot delete screen: project must have at least one screen.")
            return
        }
        val screenIndex = currentProject.screens.indexOfFirst { it.id == screenId }
        if (screenIndex == -1) {
            notifyUser("Cannot delete screen: screen not found.")
            return
        }
        val screen = currentProject.screens[screenIndex]
        execute(DeleteScreenCommand(screen, screenIndex))

        // If the deleted screen was active, switch active screen to remaining adjacent screen
        if (activeScreenId.value == screenId) {
            val remainingScreens = currentProject.screens.filter { it.id != screenId }
            val nextActiveIndex = screenIndex.coerceAtMost(remainingScreens.lastIndex)
            remainingScreens.getOrNull(nextActiveIndex)?.let { setActiveScreen(it.id) }
        }
    }

    // --- Bridge: Duplicate Screen ---
    fun duplicateScreen(screenId: ScreenId) {
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot duplicate screen: no project open.")
            return
        }
        val originalScreen = currentProject.findScreen(screenId)
        if (originalScreen == null) {
            notifyUser("Cannot duplicate screen: screen not found.")
            return
        }

        fun reIdSubtree(node: DesignNode): DesignNode {
            val freshId = context.idGenerator.nextNodeId()
            val freshChildren = node.children.map { reIdSubtree(it) }
            val freshSlots = node.slots.mapValues { (_, slotChildren) -> slotChildren.map { reIdSubtree(it) } }
            return node.copy(id = freshId, children = freshChildren, slots = freshSlots)
        }

        // Generate unique name e.g. Home_Copy, Home_Copy_2
        var candidateName = "${originalScreen.name}_Copy"
        var counter = 2
        while (currentProject.screens.any { it.name.equals(candidateName, ignoreCase = true) }) {
            candidateName = "${originalScreen.name}_Copy_$counter"
            counter++
        }

        // Generate unique route e.g. /home-copy
        var candidateRoute = "/${candidateName.lowercase().replace('_', '-')}"
        var routeCounter = 2
        while (currentProject.screens.any { it.route.equals(candidateRoute, ignoreCase = true) }) {
            candidateRoute = "/${candidateName.lowercase().replace('_', '-')}-$routeCounter"
            routeCounter++
        }

        val freshScreenId = context.idGenerator.nextScreenId()
        val duplicatedRoot = reIdSubtree(originalScreen.root)
        val duplicatedScreen = Screen(
            id = freshScreenId,
            name = candidateName,
            route = candidateRoute,
            root = duplicatedRoot,
            device = originalScreen.device
        )
        execute(DuplicateScreenCommand(duplicatedScreen))
        setActiveScreen(freshScreenId)
    }

    // --- Bridge: Reorder Screens ---
    fun reorderScreens(fromIndex: Int, toIndex: Int) {
        execute(ReorderScreensCommand(fromIndex, toIndex))
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
