package io.github.chandu4221.m3stage.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.query.canPlace
import io.github.chandu4221.m3stage.query.findNearestAcceptingAncestor
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.flatten
import io.github.chandu4221.m3stage.state.CanvasViewportState
import io.github.chandu4221.m3stage.state.EditorEvent
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.molecule.ExportDialog
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import io.github.chandu4221.m3stage.ui.template.EditorShellTemplate

/**
 * Smart Page: The ONLY stateful mediator between EditorStore and the dumb UI template.
 */
@Composable
fun EditorScreen(store: EditorStore) {
    val project by store.project.collectAsState()
    val activeScreenId by store.activeScreenId
    val selectedNodeId by store.selectedNodeId
    val lockedNodeIds by store.lockedNodeIds
    val undoStack by store.undoStack.collectAsState()
    val redoStack by store.redoStack.collectAsState()
    val isDirty by store.isDirty.collectAsState()

    var activeRailTab by remember { mutableStateOf(StudioRailTab.Parts) }
    var searchQuery by remember { mutableStateOf("") }
    var zoomPercentage by remember { mutableStateOf(100) }

    val activeScreen = project?.screens?.firstOrNull { it.id == activeScreenId }
    val selectedNode = activeScreen?.root?.let { root ->
        selectedNodeId?.let { root.findNode(it) }
    }
    val effectiveTargetNode = selectedNode ?: activeScreen?.root
    val disallowedKinds = remember(effectiveTargetNode, activeScreen?.root) {
        val root = activeScreen?.root
        if (effectiveTargetNode == null || root == null) {
            emptySet()
        } else {
            ComponentKind.entries.filterNot { kind ->
                effectiveTargetNode.findNearestAcceptingAncestor(root, kind) != null
            }.toSet()
        }
    }

    var viewportState by remember { mutableStateOf(CanvasViewportState()) }

    var showExportDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        store.events.collect { event ->
            when (event) {
                is EditorEvent.ExportSuccess -> {
                    snackbarHostState.showSnackbar("Exported ZIP to: ${event.path}")
                }

                is EditorEvent.ExportFailed -> {
                    snackbarHostState.showSnackbar("Export failed: ${event.message}")
                }

                is EditorEvent.LoadFailed -> {
                    snackbarHostState.showSnackbar(
                        message = "Failed to load project: ${event.reason}",
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                }

                is EditorEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                }

                else -> {}
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            initialPackageName = project?.basePackage ?: "com.example.app",
            screens = project?.screens ?: emptyList(),
            onDismiss = { showExportDialog = false },
            onConfirm = { customPackage ->
                showExportDialog = false
                store.exportCode(customPackage)
            }
        )
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                // 1. Undo / Redo
                if (keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.Z) {
                    if (undoStack.isNotEmpty() || redoStack.isNotEmpty()) {
                        store.redo()
                        return@onPreviewKeyEvent true
                    }
                } else if (keyEvent.isCtrlPressed && keyEvent.key == Key.Z) {
                    if (undoStack.isNotEmpty()) {
                        store.undo()
                        return@onPreviewKeyEvent true
                    }
                }

                // 2. Duplicate: Ctrl+D
                if (keyEvent.isCtrlPressed && keyEvent.key == Key.D) {
                    val currentSelected = selectedNodeId
                    if (currentSelected != null) {
                        store.duplicateNode(currentSelected)
                        return@onPreviewKeyEvent true
                    }
                }

                // 2.5. Save: Ctrl+S
                if (keyEvent.isCtrlPressed && keyEvent.key == Key.S) {
                    store.saveProject()
                    return@onPreviewKeyEvent true
                }

                // 3. Escape: deselect
                if (keyEvent.key == Key.Escape) {
                    store.selectNode(null)
                    return@onPreviewKeyEvent true
                }

                // 4. Delete / Backspace: remove node
                if (keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace) {
                    val currentSelected = selectedNodeId
                    if (currentSelected != null) {
                        store.removeNode(currentSelected)
                        return@onPreviewKeyEvent true
                    }
                }

                // 5. Arrow Up / Down in Layers tree
                if (keyEvent.key == Key.DirectionUp || keyEvent.key == Key.DirectionDown) {
                    val screen = activeScreen
                    if (screen != null) {
                        val allNodes = screen.root.flatten().filter { it.isVisible }
                        if (allNodes.isNotEmpty()) {
                            val currentIndex = allNodes.indexOfFirst { it.id == selectedNodeId }
                            val newIndex = if (keyEvent.key == Key.DirectionUp) {
                                if (currentIndex <= 0) allNodes.lastIndex else currentIndex - 1
                            } else {
                                if (currentIndex == -1 || currentIndex >= allNodes.lastIndex) 0 else currentIndex + 1
                            }
                            store.selectNode(allNodes[newIndex].id)
                            return@onPreviewKeyEvent true
                        }
                    }
                }

                false
            }
    ) {
        EditorShellTemplate(
            project = project,
            // Rail
            activeRailTab = activeRailTab,
            onRailTabSelected = { activeRailTab = it },
            isDarkMode = project?.isDarkMode ?: false,
            onToggleDarkMode = { store.toggleDarkMode() },

            // Parts Drawer
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onComponentSelected = { kind ->
                val active = activeScreen
                if (active == null) {
                    store.notifyUser("No active screen. Create or select a screen first.")
                    return@EditorShellTemplate
                }
                val startNode = (selectedNodeId?.let { active.root.findNode(it) }) ?: active.root
                val targetAncestor = startNode.findNearestAcceptingAncestor(active.root, kind)
                if (targetAncestor != null) {
                    store.addNodeToActiveScreen(targetAncestor.id, kind)
                } else {
                    store.addNodeToActiveScreen(startNode.id, kind)
                }
            },
            disallowedKinds = disallowedKinds,

            // Floating Canvas
            screens = project?.screens ?: emptyList(),
            activeScreenId = activeScreenId,
            onSelectScreen = { store.setActiveScreen(it) },
            selectedNodeId = selectedNodeId,
            onNodeClick = { nodeId -> store.selectNode(nodeId) },
            lockedNodeIds = lockedNodeIds,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            onUndo = { store.undo() },
            onRedo = { store.redo() },
            onAddScreen = {
                val count = (project?.screens?.size ?: 0) + 1
                store.addNewScreen("Screen $count", "/screen$count")
                // Automatically pan canvas to reveal the newly added screen on the right!
                val screenIndex = project?.screens?.size ?: 1
                val estimatedOffset = -((screenIndex - 1) * 460f)
                viewportState = viewportState.copy(panOffset = Offset(estimatedOffset, 0f))
            },
            onExportCode = { showExportDialog = true },
            zoomPercentage = zoomPercentage,
            isDirty = isDirty,
            onSaveProject = { store.saveProject() },
            // Inspector & Tree Actions
            selectedNode = selectedNode,
            isNodeLocked = selectedNodeId?.let { store.isNodeLocked(it) } ?: false,
            onToggleNodeLock = { nodeId -> store.toggleLock(nodeId) },
            onToggleVisibility = { nodeId -> store.toggleVisibility(nodeId) },
            onDeleteNode = { nodeId -> store.removeNode(nodeId) },

            // Viewport & Gestures
            viewportState = viewportState,
            onPointerToolChange = { tool -> viewportState = viewportState.copy(activeTool = tool) },
            onPanDelta = { delta -> viewportState = viewportState.panBy(delta) },
            onWheelZoom = { delta ->
                val newZoom = (viewportState.zoom + delta).coerceIn(0.25f, 2.5f)
                viewportState = viewportState.copy(zoom = newZoom)
            },
            onZoomIn = { viewportState = viewportState.zoomIn() },
            onZoomOut = { viewportState = viewportState.zoomOut() },
            onZoomFit = { viewportState = viewportState.zoomFit() },
            onTidy = { viewportState = viewportState.tidy() },
            store = store,
        )

        // Add this right here, inside the Box after EditorShellTemplate:
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}