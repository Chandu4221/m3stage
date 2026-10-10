package io.github.chandu4221.m3stage.ui

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
import io.github.chandu4221.m3stage.query.findNode
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

    var activeRailTab by remember { mutableStateOf(StudioRailTab.Parts) }
    var searchQuery by remember { mutableStateOf("") }
    var zoomPercentage by remember { mutableStateOf(100) }

    val activeScreen = project?.screens?.firstOrNull { it.id == activeScreenId }
    val selectedNode = activeScreen?.root?.let { root ->
        selectedNodeId?.let { root.findNode(it) }
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
                    snackbarHostState.showSnackbar("Failed to load project: ${event.reason}")
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                val targetParentId = selectedNodeId
                    ?: activeScreen?.root?.id
                    ?: return@EditorShellTemplate
                store.addNodeToActiveScreen(targetParentId, kind)
            },

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
            // Inspector
            selectedNode = selectedNode,
            isNodeLocked = selectedNodeId?.let { store.isNodeLocked(it) } ?: false,
            onToggleNodeLock = { nodeId -> store.toggleLock(nodeId) },

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