package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.ui.CanvasPanel
import io.github.chandu4221.m3stage.ui.atom.DimensionBadge
import io.github.chandu4221.m3stage.ui.atom.ToolIconButton
import io.github.chandu4221.m3stage.ui.molecule.CapsuleToolbar
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Organism: Floating rounded canvas studio workspace.
 * Floating capsule islands hover over a rounded workspace card.
 */
@Composable
fun FloatingCanvasStudio(
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceDim,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Multi-Screen Canvas Panel
            if (screens.isNotEmpty()) {
                CanvasPanel(
                    screens = screens,
                    activeScreenId = activeScreenId,
                    onSelectScreen = onSelectScreen,
                    selectedNodeId = selectedNodeId,
                    onNodeClick = onNodeClick,
                    onAddScreen = onAddScreen,
                    lockedNodeIds = lockedNodeIds,
                    projectDefaultDevice = DevicePreset.Default
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No screens in project.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Top Floating Island: Capsule Toolbars
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Island 1: Pointer Modes
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.Default.NearMe,
                        contentDescription = "Select pointer",
                        isSelected = true,
                        onClick = {}
                    )
                    ToolIconButton(
                        icon = Icons.Default.PanTool,
                        contentDescription = "Pan canvas",
                        onClick = {}
                    )
                }

                // Island 2: Add Screen & Export
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Add screen",
                        onClick = onAddScreen,
                        tooltip = "Add new screen"
                    )
                    ToolIconButton(
                        icon = Icons.Default.Download,
                        contentDescription = "Export Code",
                        onClick = onExportCode,
                        tooltip = "Export Kotlin Compose code"
                    )
                }

                // Island 3: Undo / Redo
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        isEnabled = canUndo,
                        onClick = onUndo,
                        tooltip = "Undo (Ctrl+Z)"
                    )
                    ToolIconButton(
                        icon = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        isEnabled = canRedo,
                        onClick = onRedo,
                        tooltip = "Redo (Ctrl+Y)"
                    )
                }
            }

            // Bottom-Right Floating Island: Zoom Controls
            CapsuleToolbar(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                ToolIconButton(
                    icon = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    onClick = onZoomOut
                )
                DimensionBadge(text = "$zoomPercentage%")
                ToolIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    onClick = onZoomIn
                )
                ToolIconButton(
                    icon = Icons.Default.FitScreen,
                    contentDescription = "Zoom to Fit",
                    onClick = onZoomFit
                )
            }
        }
    }
}
@Preview
@Composable
private fun FloatingCanvasStudioPreview() {
    DualThemePreview {
        Box(modifier = Modifier.size(600.dp, 400.dp)) {
            FloatingCanvasStudio(
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
                onZoomFit = {}
            )
        }
    }
}