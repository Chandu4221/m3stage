package io.github.chandu4221.m3stage.ui.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.InspectorPanel
import io.github.chandu4221.m3stage.ui.organism.FloatingCanvasStudio
import io.github.chandu4221.m3stage.ui.organism.PartsDrawer
import io.github.chandu4221.m3stage.ui.organism.StudioNavRail
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

@Composable
fun EditorShellTemplate(
    project: Project?,
    // Rail state
    activeRailTab: StudioRailTab,
    onRailTabSelected: (StudioRailTab) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    // Drawer state
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onComponentSelected: (ComponentKind) -> Unit,
    // Canvas state
    screens: List<Screen>,
    activeScreenId: ScreenId?,
    onSelectScreen: (ScreenId) -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId) -> Unit,
    lockedNodeIds: Set<NodeId>,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAddScreen: () -> Unit,
    onExportCode: () -> Unit,
    zoomPercentage: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomFit: () -> Unit,
    // Inspector state
    selectedNode: DesignNode?,
    isNodeLocked: Boolean,
    onToggleNodeLock: (NodeId) -> Unit,
    modifier: Modifier = Modifier,
    viewportState: io.github.chandu4221.m3stage.state.CanvasViewportState,
    onPointerToolChange: (io.github.chandu4221.m3stage.state.CanvasPointerTool) -> Unit,
    onPanDelta: (androidx.compose.ui.geometry.Offset) -> Unit,
    onWheelZoom: (Float) -> Unit,
    onTidy: () -> Unit,
    store: EditorStore,
) {
    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Far-Left Studio Navigation Rail
        StudioNavRail(
            activeTab = activeRailTab,
            onTabSelected = onRailTabSelected,
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode
        )

        // 2. Expandable Drawer
        PartsDrawer(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onComponentSelected = onComponentSelected
        )

        // 3. Central Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            FloatingCanvasStudio(
                screens = screens,
                activeScreenId = activeScreenId,
                onSelectScreen = onSelectScreen,
                selectedNodeId = selectedNodeId,
                onNodeClick = onNodeClick,
                lockedNodeIds = lockedNodeIds,
                canUndo = canUndo,
                canRedo = canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onAddScreen = onAddScreen,
                onExportCode = onExportCode,
                viewportState = viewportState,
                onPointerToolChange = onPointerToolChange,
                onPanDelta = onPanDelta,
                onWheelZoom = onWheelZoom,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onZoomFit = onZoomFit,
                onTidy = onTidy
            )
        }

        // 4. Right Inspector Surface
        Surface(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            InspectorPanel(
                project = project,
                activeScreenId = activeScreenId?.value,
                selectedNodeId = selectedNodeId,
                isLocked = isNodeLocked,
                store = store
            )
        }
    }
}