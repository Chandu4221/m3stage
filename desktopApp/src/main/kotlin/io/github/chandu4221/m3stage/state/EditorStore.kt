package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
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

    override fun close() {
        storeScope.cancel()
        sessionDelegate.close()
    }

    // --- Bridge: Add Node ---
    fun addNodeToActiveScreen(parentId: NodeId, kind: ComponentKind) {
        val screenId = activeScreenId.value ?: return
        val currentProject = project.value ?: return
        val activeScreen = currentProject.findScreen(screenId) ?: return

        fun buildNode(targetKind: ComponentKind): DesignNode {
            val def = ComponentCatalog[targetKind]
            val defaultChildren = when (targetKind) {
                ComponentKind.Button -> listOf(buildNode(ComponentKind.Text))
                ComponentKind.Scaffold -> listOf(
                    buildNode(ComponentKind.TopAppBar),
                    buildNode(ComponentKind.Column)
                )

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

        // If target parent is a Scaffold and adding content (not TopAppBar), route into its child Column if present
        val targetNode = activeScreen.root.findNode(parentId)
        val resolvedParentId = if (targetNode?.kind == ComponentKind.Scaffold && kind != ComponentKind.TopAppBar) {
            targetNode.children.firstOrNull { it.kind == ComponentKind.Column }?.id ?: parentId
        } else {
            parentId
        }

        val newNode = buildNode(kind)
        execute(AddNodeCommand(screenId, resolvedParentId, newNode))
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

        val screenId = idGen.nextScreenId()
        val newScreen = Screen(
            id = screenId,
            name = name,
            route = route,
            device = device,
            root = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Scaffold,
                props = scaffoldDef.createDefaultProps(),
                children = listOf(topBarNode, contentColumnNode)
            )
        )
        execute(AddScreenCommand(newScreen))
        setActiveScreen(screenId)
    }

    // --- Bridge: Update Project Theme ---
    fun updateSeedColor(newSeed: Long) {
        val current = project.value ?: return
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
        val current = project.value ?: return
        execute(
            UpdateThemeCommand(
                oldSeedColor = current.seedColor,
                oldIsDarkMode = current.isDarkMode,
                newSeedColor = current.seedColor,
                newIsDarkMode = !current.isDarkMode
            )
        )
    }

}