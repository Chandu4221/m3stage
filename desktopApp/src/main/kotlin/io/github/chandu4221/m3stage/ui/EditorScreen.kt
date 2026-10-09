package io.github.chandu4221.m3stage.ui

import androidx.compose.runtime.*
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.state.CanvasViewportState
import io.github.chandu4221.m3stage.state.EditorStore
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

    EditorShellTemplate(
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
            viewportState = viewportState.copy(panOffset = androidx.compose.ui.geometry.Offset(estimatedOffset, 0f))
        },
        onExportCode = { store.exportCode() },
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
    )
}