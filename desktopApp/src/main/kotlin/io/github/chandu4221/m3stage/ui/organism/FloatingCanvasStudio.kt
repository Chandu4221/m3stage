package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportController
import io.github.chandu4221.m3stage.state.CanvasViewportDelegate
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.atom.DimensionBadge
import io.github.chandu4221.m3stage.ui.atom.StageIcons
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
    onSelectScreen: (ScreenId?) -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId?) -> Unit,
    selectedNodeIds: Set<NodeId> = selectedNodeId?.let { setOf(it) } ?: emptySet(),
    onToggleNodeSelection: (NodeId) -> Unit = {},
    onContextMenu: ((ScreenId, NodeId?, androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    lockedNodeIds: Set<NodeId>,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAddScreen: () -> Unit,
    onExportCode: () -> Unit,
    viewportController: CanvasViewportController,
    projectName: String? = null,
    isDirty: Boolean = false,
    onSaveProject: () -> Unit = {},
    onRenameScreen: (ScreenId, String) -> Unit = { _, _ -> },
    onDeleteScreen: (ScreenId) -> Unit = {},
    onDuplicateScreen: (ScreenId) -> Unit = {},
    onMoveScreen: (ScreenId, Float, Float) -> Unit = { _, _, _ -> },
    onTidy: () -> Unit = {},
    onReorderScreens: (Int, Int) -> Unit = { _, _ -> },
    onDeviceChange: (ScreenId, DevicePreset) -> Unit = { _, _ -> },
    onOrientationToggle: (ScreenId) -> Unit = {},
    isCodePreviewOpen: Boolean = false,
    onToggleCodePreview: () -> Unit = {},
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    isLeftPanelOpen: Boolean = true,
    onToggleLeftPanel: () -> Unit = {},
    isRightPanelOpen: Boolean = true,
    onToggleRightPanel: () -> Unit = {},
    isFullWidth: Boolean = false,
    onToggleFullWidth: () -> Unit = {},
    store: EditorStore? = null,
    modifier: Modifier = Modifier
) {
    val viewportState = viewportController.viewportState

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
                    selectedNodeIds = selectedNodeIds,
                    onToggleNodeSelection = onToggleNodeSelection,
                    onContextMenu = onContextMenu,
                    viewportController = viewportController,
                    lockedNodeIds = lockedNodeIds,
                    projectDefaultDevice = DevicePreset.Default,
                    onRenameScreen = onRenameScreen,
                    onDeleteScreen = onDeleteScreen,
                    onDuplicateScreen = onDuplicateScreen,
                    onMoveScreen = onMoveScreen,
                    onReorderScreens = onReorderScreens,
                    onDeviceChange = onDeviceChange,
                    onOrientationToggle = onOrientationToggle,
                    draggedComponentKind = draggedComponentKind,
                    onDropOnScreen = onDropOnScreen,
                    store = store
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
                        isSelected = viewportState.value.activeTool == CanvasPointerTool.Select,
                        onClick = { viewportController.setActiveTool(CanvasPointerTool.Select) },
                        tooltip = "Selection Pointer (V)"
                    )
                    ToolIconButton(
                        icon = Icons.Default.PanTool,
                        contentDescription = "Hand tool",
                        isSelected = viewportState.value.activeTool == CanvasPointerTool.Pan,
                        onClick = { viewportController.setActiveTool(CanvasPointerTool.Pan) },
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

                // Island 4: Full Width View
                CapsuleToolbar {
                    ToolIconButton(
                        icon = StageIcons.WidthFull,
                        contentDescription = "Full Width",
                        isSelected = isFullWidth,
                        onClick = onToggleFullWidth,
                        tooltip = if (isFullWidth) "Exit Full Width" else "Full Width Canvas"
                    )
                }
            }

            // Top-Left Floating Button: Toggle Left Panel
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp)
            ) {
                CapsuleToolbar {
                    ToolIconButton(
                        icon = if (isLeftPanelOpen) StageIcons.LeftPanelClose else StageIcons.LeftPanelOpen,
                        contentDescription = if (isLeftPanelOpen) "Collapse Left Panel" else "Open Left Panel",
                        isSelected = isLeftPanelOpen,
                        onClick = onToggleLeftPanel,
                        tooltip = if (isLeftPanelOpen) "Collapse Left Panel" else "Open Left Panel"
                    )
                }
            }

            // Top-Right Floating Button: Toggle Right Panel
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                CapsuleToolbar {
                    ToolIconButton(
                        icon = if (isRightPanelOpen) StageIcons.RightPanelClose else StageIcons.RightPanelOpen,
                        contentDescription = if (isRightPanelOpen) "Collapse Inspector" else "Open Inspector",
                        isSelected = isRightPanelOpen,
                        onClick = onToggleRightPanel,
                        tooltip = if (isRightPanelOpen) "Collapse Inspector" else "Open Inspector"
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
                        onClick = {
                            viewportController.tidy()
                            onTidy()
                        },
                        tooltip = "Tidy & Center Artboards"
                    )
                }

                // Zoom Capsule
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        onClick = { viewportController.zoomOut() },
                        tooltip = "Zoom Out"
                    )
                    DimensionBadge(text = "${viewportState.value.zoomPercentage}%")
                    ToolIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        onClick = { viewportController.zoomIn() },
                        tooltip = "Zoom In"
                    )
                    ToolIconButton(
                        icon = Icons.Default.FitScreen,
                        contentDescription = "Zoom to Fit",
                        onClick = {
                            val bounds = computeScreensContentBounds(screens)
                            viewportController.zoomFit(contentBounds = bounds)
                        },
                        tooltip = "Zoom to Fit"
                    )
                }

                // Code Preview Toggle Capsule
                CapsuleToolbar {
                    ToolIconButton(
                        icon = Icons.Default.Code,
                        contentDescription = "Toggle Code Preview",
                        isSelected = isCodePreviewOpen,
                        onClick = onToggleCodePreview,
                        tooltip = "Toggle Code Preview"
                    )
                }
            }
        }
    }
}

private fun computeScreensContentBounds(
    screens: List<Screen>,
    defaultDevice: DevicePreset = DevicePreset.Default
): Rect? {
    if (screens.isEmpty()) return null
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE
    for (screen in screens) {
        val w = screen.resolveWidthDp(defaultDevice).toFloat()
        val h = screen.resolveHeightDp(defaultDevice).toFloat()
        minX = minOf(minX, screen.canvasX)
        minY = minOf(minY, screen.canvasY)
        maxX = maxOf(maxX, screen.canvasX + w)
        maxY = maxOf(maxY, screen.canvasY + h)
    }
    if (maxX <= minX || maxY <= minY) return null
    val padding = 64f
    return Rect(
        left = minX - padding,
        top = minY - padding - 48f,
        right = maxX + padding,
        bottom = maxY + padding
    )
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
                viewportController = CanvasViewportDelegate()
            )
        }
    }
}