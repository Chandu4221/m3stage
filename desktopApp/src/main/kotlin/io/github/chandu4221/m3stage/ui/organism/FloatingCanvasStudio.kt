package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportState
import io.github.chandu4221.m3stage.ui.CanvasPanel
import io.github.chandu4221.m3stage.ui.atom.DimensionBadge
import io.github.chandu4221.m3stage.ui.atom.ToolIconButton
import io.github.chandu4221.m3stage.ui.molecule.CapsuleToolbar
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Organism: Floating rounded canvas studio workspace with live viewport transforms.
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
    viewportState: CanvasViewportState,
    onPointerToolChange: (CanvasPointerTool) -> Unit,
    onPanDelta: (Offset) -> Unit,
    onWheelZoom: (Float) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomFit: () -> Unit,
    onTidy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceDim,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live Transformable Canvas
            if (screens.isNotEmpty()) {
                CanvasPanel(
                    screens = screens,
                    activeScreenId = activeScreenId,
                    onSelectScreen = onSelectScreen,
                    selectedNodeId = selectedNodeId,
                    onNodeClick = onNodeClick,
                    viewportState = viewportState,
                    onPanDelta = onPanDelta,
                    onWheelZoom = onWheelZoom,
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
                // Island 1: Pointer Modes (Select vs. Pan)
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.Default.NearMe,
                        contentDescription = "Select tool",
                        isSelected = viewportState.activeTool == CanvasPointerTool.Select,
                        onClick = { onPointerToolChange(CanvasPointerTool.Select) },
                        tooltip = "Selection Pointer (V)"
                    )
                    ToolIconButton(
                        icon = Icons.Default.PanTool,
                        contentDescription = "Hand tool",
                        isSelected = viewportState.activeTool == CanvasPointerTool.Pan,
                        onClick = { onPointerToolChange(CanvasPointerTool.Pan) },
                        tooltip = "Hand Pan Tool (H / Hold Space)"
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

            // Bottom-Right Floating Island: Tidy + Zoom Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tidy Capsule
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                        contentDescription = "Tidy Artboards",
                        onClick = onTidy,
                        tooltip = "Tidy & Center Artboards"
                    )
                }

                // Zoom Capsule
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        onClick = onZoomOut,
                        tooltip = "Zoom Out"
                    )
                    DimensionBadge(text = "${viewportState.zoomPercentage}%")
                    ToolIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        onClick = onZoomIn,
                        tooltip = "Zoom In"
                    )
                    ToolIconButton(
                        icon = Icons.Default.FitScreen,
                        contentDescription = "Zoom to Fit",
                        onClick = onZoomFit,
                        tooltip = "Reset Zoom (100%)"
                    )
                }
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
                viewportState = CanvasViewportState(),
                onPointerToolChange = {},
                onPanDelta = {},
                onWheelZoom = {},
                onZoomIn = {},
                onZoomOut = {},
                onZoomFit = {},
                onTidy = {}
            )
        }
    }
}