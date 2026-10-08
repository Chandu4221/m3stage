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
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.ui.organism.FloatingCanvasStudio
import io.github.chandu4221.m3stage.ui.organism.PartsDrawer
import io.github.chandu4221.m3stage.ui.organism.StudioNavRail
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Template: Modern visual builder workspace layout.
 * Assembles StudioNavRail + Drawer + FloatingCanvasStudio + Inspector.
 * 100% dumb presentational component.
 */
@Composable
fun EditorShellTemplate(
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
    activeScreenId: io.github.chandu4221.m3stage.model.ScreenId?,
    onSelectScreen: (io.github.chandu4221.m3stage.model.ScreenId) -> Unit,
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
) {
    Row(
        modifier = modifier
            .fillMaxSize()
    ) {
        // 1. Far-Left Studio Navigation Rail
        StudioNavRail(
            activeTab = activeRailTab,
            onTabSelected = onRailTabSelected,
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode
        )

        // 2. Sliding Left Drawer (Parts or Tree)
        if (activeRailTab == StudioRailTab.Parts) {
            PartsDrawer(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onComponentSelected = onComponentSelected
            )
        }

        // 3. Center Floating Canvas Workspace
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
                zoomPercentage = zoomPercentage,
                onZoomIn = onZoomIn,
                onZoomOut = onZoomOut,
                onZoomFit = onZoomFit
            )
        }

        // 4. Right Inspector Surface
        Surface(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            // Placeholder inspector slot (will house visual controls organism in Phase 4)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Inspector",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedNode != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = selectedNode.kind.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Lock Node", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = isNodeLocked,
                                    onCheckedChange = { onToggleNodeLock(selectedNode.id) }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedCard(
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "No Selection",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select an element from the canvas to inspect its properties.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun EditorShellTemplatePreview() {
    DualThemePreview {
        Box(modifier = Modifier.size(900.dp, 500.dp)) {
            EditorShellTemplate(
                activeRailTab = StudioRailTab.Parts,
                onRailTabSelected = {},
                isDarkMode = false,
                onToggleDarkMode = {},
                searchQuery = "",
                onSearchQueryChange = {},
                onComponentSelected = {},
                screens = emptyList(),
                activeScreenId = null,
                onSelectScreen = {},
                selectedNodeId = null,
                onNodeClick = {},
                lockedNodeIds = emptySet(),
                canUndo = true,
                canRedo = false,
                onUndo = {},
                onRedo = {},
                onAddScreen = {},
                onExportCode = {},
                zoomPercentage = 100,
                onZoomIn = {},
                onZoomOut = {},
                onZoomFit = {},
                selectedNode = null,
                isNodeLocked = false,
                onToggleNodeLock = {}
            )
        }
    }
}