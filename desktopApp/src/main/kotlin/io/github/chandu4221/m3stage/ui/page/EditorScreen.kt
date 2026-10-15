package io.github.chandu4221.m3stage.ui.page

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.query.findNearestAcceptingAncestor
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.state.CodeGenDelegate
import io.github.chandu4221.m3stage.state.EditorEvent
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.state.rememberCanvasViewportController
import io.github.chandu4221.m3stage.state.rememberShellPanelsController
import io.github.chandu4221.m3stage.ui.molecule.ExportDialog
import io.github.chandu4221.m3stage.ui.template.EditorShellTemplate

/**
 * Smart Page: The ONLY stateful mediator between EditorStore and the dumb UI template.
 */
@Composable
fun EditorScreen(store: EditorStore) {
    val projectState = store.project.collectAsState()
    val project by projectState
    val activeScreenId by store.activeScreenId
    val selectedNodeId by store.selectedNodeId
    val lockedNodeIds by store.lockedNodeIds
    val undoStack by store.undoStack.collectAsState()
    val redoStack by store.redoStack.collectAsState()

    val panels = rememberShellPanelsController()
    var searchQuery by remember { mutableStateOf("") }
    val activeScreen = project?.screens?.firstOrNull { it.id == activeScreenId }

    val disallowedKinds by remember {
        derivedStateOf {
            computeDisallowedKinds(projectState.value, store.activeScreenId.value, store.selectedNodeId.value)
        }
    }

    val viewportController = rememberCanvasViewportController()
    var showExportDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var draggedComponentKind by remember { mutableStateOf<ComponentKind?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val codeGen = remember(store.codeGenerator) { CodeGenDelegate(store.codeGenerator, coroutineScope) }
    DisposableEffect(codeGen) { onDispose { codeGen.close() } }

    LaunchedEffect(panels.isBottomOpen, project, activeScreenId) {
        if (!panels.isBottomOpen) {
            codeGen.close()
            return@LaunchedEffect
        }
        val currentProject = project ?: return@LaunchedEffect
        val currentActiveScreenId = activeScreenId ?: return@LaunchedEffect
        val targetScreen = currentProject.screens.firstOrNull { it.id == currentActiveScreenId } ?: return@LaunchedEffect
        codeGen.request(targetScreen, currentProject)
    }

    LaunchedEffect(Unit) {
        store.events.collect { event ->
            when (event) {
                is EditorEvent.ExportSuccess -> snackbarHostState.showSnackbar("Exported ZIP to: ${event.path}")
                is EditorEvent.ExportFailed -> snackbarHostState.showSnackbar("Export failed: ${event.message}")
                is EditorEvent.LoadFailed -> snackbarHostState.showSnackbar("Failed to load project: ${event.reason}", duration = SnackbarDuration.Long)
                is EditorEvent.ShowError -> snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Long)
                else -> {}
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            initialPackageName = project?.basePackage ?: "com.example.app",
            screens = project?.screens ?: emptyList(),
            onDismiss = { showExportDialog = false },
            onConfirm = { customPackage -> showExportDialog = false; store.exportCode(customPackage) }
        )
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val shortcutActions = remember(draggedComponentKind) {
        EditorShortcutActions(
            isPaletteDragging = { draggedComponentKind != null },
            onCancelPaletteDrag = { draggedComponentKind = null }
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable()
            .editorKeyboardShortcuts(store = store, panels = panels, viewportController = viewportController, actions = shortcutActions)
    ) {
        LaunchedEffect(maxWidth, maxHeight) {
            panels.updateAvailableSpace(maxWidth.value, maxHeight.value)
        }

        EditorShellTemplate(
            project = project,
            panels = panels,
            isDarkMode = project?.isDarkMode ?: false,
            onToggleDarkMode = { store.toggleDarkMode() },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onComponentSelected = { kind ->
                val active = activeScreen ?: return@EditorShellTemplate store.notifyUser("No active screen. Create or select a screen first.")
                val start = (selectedNodeId?.let { active.root.findNode(it) }) ?: active.root
                val target = start.findNearestAcceptingAncestor(active.root, kind) ?: start
                store.addNodeToActiveScreen(target.id, kind)
            },
            disallowedKinds = disallowedKinds,
            draggedComponentKind = draggedComponentKind,
            onComponentDragStart = { draggedComponentKind = it },
            onComponentDragEnd = { draggedComponentKind = null },
            onDropOnScreen = { screenId, kind ->
                store.setActiveScreen(screenId)
                project?.screens?.firstOrNull { it.id == screenId }?.let { targetScreen ->
                    val start = (selectedNodeId?.let { targetScreen.root.findNode(it) }) ?: targetScreen.root
                    val target = start.findNearestAcceptingAncestor(targetScreen.root, kind) ?: targetScreen.root
                    store.addNodeToActiveScreen(target.id, kind)
                }
                draggedComponentKind = null
            },
            screens = project?.screens ?: emptyList(),
            activeScreenId = activeScreenId,
            onSelectScreen = { store.setActiveScreen(it) },
            selectedNodeId = selectedNodeId,
            onNodeClick = { nodeId ->
                store.selectNode(nodeId)
                if (nodeId != null) {
                    activeScreen?.root?.let { root -> store.setDrillAnchor(root.toTreeInfo()[nodeId]?.parent ?: root.id) }
                }
            },
            lockedNodeIds = lockedNodeIds,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            onAddScreen = {
                val count = (project?.screens?.size ?: 0) + 1
                store.addNewScreen("Screen$count", "/screen$count")
                val targetX = store.project.value?.screens?.lastOrNull()?.canvasX ?: 0f
                val currentPan = viewportController.viewportState.value.panOffset
                viewportController.panBy(Offset(-targetX - currentPan.x, 0f))
            },
            onExportCode = { showExportDialog = true },
            codeGenState = codeGen.previewState,
            isNodeLocked = selectedNodeId?.let { store.isNodeLocked(it) } ?: false,
            viewportController = viewportController,
            store = store,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        )
    }
}