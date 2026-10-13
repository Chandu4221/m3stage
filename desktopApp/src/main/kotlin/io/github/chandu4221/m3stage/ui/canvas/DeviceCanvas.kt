package io.github.chandu4221.m3stage.ui.canvas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.adapter.renderer.selection.SelectionChrome
import io.github.chandu4221.m3stage.adapter.renderer.selection.selectionPointerInput
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.ui.device.DeviceFrame
import io.github.chandu4221.m3stage.ui.device.DeviceOrientation

/**
 * Sealed Device Canvas ("iframe" sandbox).
 * Encapsulates the device frame bezel, Compose design renderer, local node bounds registry,
 * selection overlay, and transparent pointer input layer.
 *
 * Invariants:
 * - Local geometry and bounds are strictly internal.
 * - Zoom is used solely for stroke-width compensation inside [SelectionChrome].
 * - No outer clickable wrapping the content when in standard selection mode.
 */
@Composable
fun DeviceCanvas(
    screen: Screen,
    device: DevicePreset,
    orientation: DeviceOrientation,
    showFrame: Boolean,
    isActive: Boolean,
    selectedNodeId: NodeId?,
    lockedNodeIds: Set<NodeId>,
    onActivate: () -> Unit,
    onSelectNode: (NodeId?) -> Unit,
    zoomProvider: () -> Float,
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // Encapsulated Artboard-Local Selection & Bounds State
    val registry = remember(screen.id) { NodeBoundsRegistry() }
    val treeInfo = remember(screen.root) { screen.root.toTreeInfo() }
    val anchorState = remember(screen.id) { mutableStateOf<NodeId?>(null) }
    var hoveredNodeId by remember(screen.id) { mutableStateOf<NodeId?>(null) }
    var artboardOrigin by remember { mutableStateOf(Offset.Zero) }

    val selectedNode = remember(selectedNodeId, screen.root) {
        selectedNodeId?.let { screen.root.findNode(it) }
    }

    Box(
        modifier = modifier
            .then(
                if (draggedComponentKind != null) {
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onDropOnScreen(screen.id, draggedComponentKind) }
                        )
                        .drawBehind {
                            val strokeWidth = 3.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFF388E3C), // Drop target green highlight
                                size = size,
                                cornerRadius = CornerRadius(24.dp.toPx()),
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                )
                            )
                        }
                } else {
                    Modifier
                }
            )
    ) {
        DeviceFrame(
            preset = device,
            orientation = orientation,
            showFrame = showFrame
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coords ->
                        if (coords.isAttached) {
                            artboardOrigin = coords.positionInRoot()
                        }
                    }
            ) {
                // 1. Rendered components (below pointer input layer)
                DesignRenderer.Render(
                    node = screen.root,
                    selectedNodeId = selectedNodeId,
                    onNodeClick = onSelectNode,
                    lockedNodeIds = lockedNodeIds,
                    registry = registry,
                    artboardOrigin = { artboardOrigin }
                )

                // 2. Figma-style selection & hover chrome
                SelectionChrome(
                    registry = registry,
                    hoveredNodeId = hoveredNodeId,
                    selectedNodeId = selectedNodeId,
                    selectedNode = selectedNode,
                    zoomProvider = zoomProvider,
                    modifier = Modifier.fillMaxSize()
                )

                // 3. Top transparent pointer overlay (intercepts gestures before child buttons/widgets)
                val hoverCursor = if (hoveredNodeId != null) {
                    ToolCursorMapper.resolveCursor(CanvasPointerTool.Select, CanvasCursorOverride.ClickableHover)
                } else {
                    ToolCursorMapper.resolveCursor(CanvasPointerTool.Select)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerHoverIcon(hoverCursor)
                        .selectionPointerInput(
                            registry = registry,
                            treeInfo = treeInfo,
                            screenRootId = screen.root.id,
                            anchorState = anchorState,
                            onSelectNode = { target ->
                                onActivate()
                                onSelectNode(target)
                            },
                            onHoverChange = { hoveredNodeId = it }
                        )
                )
            }
        }
    }
}
