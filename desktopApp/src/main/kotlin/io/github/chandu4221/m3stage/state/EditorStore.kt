package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.query.findAcceptingSlot
import io.github.chandu4221.m3stage.query.findNearestAcceptingAncestor
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
    val clipboardDelegate = ClipboardDelegate()

    // Scope for observing state changes
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastObservedProjectId: ProjectId? = null

    init {
        // Auto-select the first screen whenever a new project is loaded or created
        storeScope.launch {
            project.collect { currentProject ->
                if (currentProject != null && currentProject.id != lastObservedProjectId) {
                    lastObservedProjectId = currentProject.id
                    if (activeScreenId.value == null) {
                        currentProject.screens.firstOrNull()?.let { firstScreen ->
                            setActiveScreen(firstScreen.id)
                        }
                    }
                }
            }
        }
    }

    // --- SelectionState ---
    override val selectedNodeId = selectionDelegate.selectedNodeId
    override val activeScreenId = selectionDelegate.activeScreenId
    override val lockedNodeIds = selectionDelegate.lockedNodeIds
    override val drillAnchorNodeId = selectionDelegate.drillAnchorNodeId
    override fun selectNode(id: NodeId?) = selectionDelegate.selectNode(id)
    override fun setActiveScreen(id: ScreenId?) = selectionDelegate.setActiveScreen(id)
    override fun toggleLock(id: NodeId) = selectionDelegate.toggleLock(id)
    override fun isNodeLocked(id: NodeId) = selectionDelegate.isNodeLocked(id)
    override fun setDrillAnchor(id: NodeId?) = selectionDelegate.setDrillAnchor(id)
    override fun popSelectionAnchor(
        treeInfo: Map<NodeId, io.github.chandu4221.m3stage.adapter.renderer.selection.NodeTreeInfo>?,
        screenRootId: NodeId?
    ) = selectionDelegate.popSelectionAnchor(treeInfo, screenRootId)

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
    override fun createNewProject() {
        sessionDelegate.createNewProject()
        project.value?.let { proj ->
            lastObservedProjectId = proj.id
        }
    }
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

        val freshSubtree = reIdSubtree(originalNode, context.idGenerator)
        execute(DuplicateNodeCommand(screenId, nodeId, freshSubtree))
        selectNode(freshSubtree.id)
    }

    // --- Bridge: Add New Screen ---
    fun addNewScreen(name: String, route: String, device: DevicePreset? = null) {
        val currentProject = project.value
        val idGen = context.idGenerator
        val rootNode = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val screenId = idGen.nextScreenId()
        val defaultDevice = currentProject?.defaultDevice ?: DevicePreset.Default
        val resolvedWidth = (device ?: defaultDevice).widthDp.toFloat()
        val nextX = if (currentProject != null && currentProject.screens.isNotEmpty()) {
            currentProject.screens.maxOf { it.canvasX + (it.device ?: defaultDevice).widthDp } + 64f
        } else {
            0f
        }
        val newScreen = Screen(
            id = screenId,
            name = name,
            route = route,
            device = device,
            canvasX = nextX,
            canvasY = 0f,
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
        val duplicatedRoot = reIdSubtree(originalScreen.root, context.idGenerator)
        val defaultDevice = currentProject.defaultDevice
        val nextX = currentProject.screens.maxOf { it.canvasX + it.resolveWidthDp(defaultDevice) } + 64f
        val duplicatedScreen = Screen(
            id = freshScreenId,
            name = candidateName,
            route = candidateRoute,
            root = duplicatedRoot,
            device = originalScreen.device,
            orientation = originalScreen.orientation,
            canvasX = nextX,
            canvasY = originalScreen.canvasY
        )
        execute(DuplicateScreenCommand(duplicatedScreen))
        setActiveScreen(freshScreenId)
    }

    // --- Bridge: Move Screen (Canvas 2D) ---
    fun moveScreen(screenId: ScreenId, newX: Float, newY: Float) {
        val screen = project.value?.findScreen(screenId)
        val oldX = screen?.canvasX ?: 0f
        val oldY = screen?.canvasY ?: 0f
        execute(MoveScreenCommand(screenId, oldX, oldY, newX, newY))
    }

    // --- Bridge: Update Screen Device & Orientation ---
    fun updateScreenDevice(screenId: ScreenId, newDevice: DevicePreset) {
        val screen = project.value?.findScreen(screenId) ?: return
        execute(
            UpdateScreenMetaCommand(
                screenId = screenId,
                oldDevice = screen.device,
                oldOrientation = screen.orientation,
                newDevice = newDevice,
                newOrientation = screen.orientation
            )
        )
    }

    fun toggleScreenOrientation(screenId: ScreenId) {
        val screen = project.value?.findScreen(screenId) ?: return
        val newOrientation = if (screen.orientation == DeviceOrientation.Portrait) {
            DeviceOrientation.Landscape
        } else {
            DeviceOrientation.Portrait
        }
        execute(
            UpdateScreenMetaCommand(
                screenId = screenId,
                oldDevice = screen.device,
                oldOrientation = screen.orientation,
                newDevice = screen.device,
                newOrientation = newOrientation
            )
        )
    }

    // --- Bridge: Tidy Screens (Horizontal Layout) ---
    fun tidyScreens() {
        val currentProject = project.value ?: return
        if (currentProject.screens.isEmpty()) return

        val defaultDevice = currentProject.defaultDevice
        val oldPositions = currentProject.screens.associate { it.id to (it.canvasX to it.canvasY) }
        val newPositions = mutableMapOf<ScreenId, Pair<Float, Float>>()
        var currentX = 0f
        for (screen in currentProject.screens) {
            newPositions[screen.id] = currentX to 0f
            val width = screen.resolveWidthDp(defaultDevice).toFloat()
            currentX += width + 64f
        }
        execute(TidyScreensCommand(oldPositions, newPositions))
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

    // --- Bridge: Update Project Metadata ---
    fun updateProjectMetadata(name: String, basePackage: String) {
        val current = project.value
        if (current == null) {
            notifyUser("Cannot update project metadata: no project open.")
            return
        }
        val trimmedName = name.trim()
        val trimmedPackage = basePackage.trim()
        if (!Project.isValidProjectName(trimmedName)) {
            notifyUser("Project name cannot be blank.")
            return
        }
        if (!Project.isValidPackageName(trimmedPackage)) {
            notifyUser("Invalid base package '$trimmedPackage'. Must follow standard Java/Kotlin package naming rules.")
            return
        }
        if (current.name == trimmedName && current.basePackage == trimmedPackage) {
            return
        }
        execute(
            UpdateProjectMetaCommand(
                oldName = current.name,
                oldBasePackage = current.basePackage,
                newName = trimmedName,
                newBasePackage = trimmedPackage
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

    // --- Bridge: Property Operations ---
    fun updateProp(nodeId: NodeId, propId: PropertyId, newValue: PropertyValue) {
        val screenId = activeScreenId.value ?: return
        val currentProject = project.value ?: return
        val screen = currentProject.findScreen(screenId) ?: return
        val node = screen.root.findNode(nodeId) ?: return
        val oldValue = node.props[propId]
        execute(UpdatePropCommand(screenId, nodeId, propId, oldValue, newValue))
    }

    // --- Bridge: Clipboard Operations ---
    fun copyNode(nodeId: NodeId) {
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot copy: no active screen selected.")
            return
        }
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot copy: no project open.")
            return
        }
        val screen = currentProject.findScreen(screenId)
        if (screen == null) {
            notifyUser("Cannot copy: active screen not found.")
            return
        }
        val originalNode = screen.root.findNode(nodeId)
        if (originalNode == null) {
            notifyUser("Cannot copy: node not found.")
            return
        }
        val freshSubtree = reIdSubtree(originalNode, context.idGenerator)
        clipboardDelegate.setClip(freshSubtree, screenId)
    }

    fun cutNode(nodeId: NodeId) {
        copyNode(nodeId)
        removeNode(nodeId)
    }

    fun pasteNode() {
        val clip = clipboardDelegate.clip.value
        if (clip == null) {
            notifyUser("Clipboard is empty.")
            return
        }
        val screenId = activeScreenId.value
        if (screenId == null) {
            notifyUser("Cannot paste: no active screen selected.")
            return
        }
        val currentProject = project.value
        if (currentProject == null) {
            notifyUser("Cannot paste: no project open.")
            return
        }
        val screen = currentProject.findScreen(screenId)
        if (screen == null) {
            notifyUser("Cannot paste: active screen not found.")
            return
        }

        val startNode = selectedNodeId.value?.let { screen.root.findNode(it) } ?: screen.root
        val targetParent = startNode.findNearestAcceptingAncestor(screen.root, clip.kind)
        if (targetParent == null) {
            notifyUser("Cannot paste ${clip.kind.displayName} here.")
            return
        }

        val parentDef = ComponentCatalog.find(targetParent.kind)
        val acceptingSlotId = parentDef?.findAcceptingSlot(clip.kind)

        val freshSubtree = reIdSubtree(clip, context.idGenerator)
        execute(AddNodeCommand(screenId, targetParent.id, freshSubtree, acceptingSlotId))
        selectNode(freshSubtree.id)
    }

    override fun close() {
        sessionDelegate.flushSave()
        sessionDelegate.close()
        historyDelegate.close()
        storeScope.cancel()
    }
}

/**
 * Pure subtree ID regeneration. Assigns a fresh NodeId to every node in the subtree
 * while preserving all other properties (props, modifiers, visibility, isPrefilled, etc.).
 */
fun reIdSubtree(node: DesignNode, idGen: IdGenerator): DesignNode {
    val freshId = idGen.nextNodeId()
    val freshChildren = node.children.map { reIdSubtree(it, idGen) }
    val freshSlots = node.slots.mapValues { (_, slotChildren) -> slotChildren.map { reIdSubtree(it, idGen) } }
    return node.copy(id = freshId, children = freshChildren, slots = freshSlots)
}
