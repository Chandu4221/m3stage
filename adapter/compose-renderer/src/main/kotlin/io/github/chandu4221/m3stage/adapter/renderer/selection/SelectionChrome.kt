package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import kotlin.math.roundToInt

private val FigmaBlue = Color(0xFF0D99FF)

/**
 * Overlay drawn directly above the artboard content.
 * Renders:
 * - Hover outline: dotted hairline stroke on the deepest node under cursor.
 * - Selection outline: solid 2.dp stroke around the selected node.
 * - 4 corner handles (8dp x 8dp white fill + blue border).
 * - Dimension pill ("W <dp> · H <dp>").
 * - Component name pill.
 *
 * All strokes are unscaled via [zoomProvider] so that hairlines stay crisp at any zoom level.
 */
@Composable
fun SelectionChrome(
    registry: NodeBoundsRegistry,
    hoveredNodeId: NodeId?,
    selectedNodeId: NodeId?,
    selectedNode: DesignNode?,
    selectedNodeIds: Set<NodeId> = selectedNodeId?.let { setOf(it) } ?: emptySet(),
    draggedNodeId: NodeId? = null,
    dropTarget: DropTargetResult? = null,
    zoomProvider: () -> Float = { 1f },
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val allBounds = registry.all()

    val targetSelectedIds = if (selectedNodeIds.isNotEmpty()) selectedNodeIds else (selectedNodeId?.let { setOf(it) } ?: emptySet())
    val primaryId = selectedNodeId ?: targetSelectedIds.lastOrNull()
    val selectedBounds = primaryId?.let { allBounds[it] }
    val hoveredBounds = if (hoveredNodeId != null && hoveredNodeId !in targetSelectedIds) {
        allBounds[hoveredNodeId]
    } else {
        null
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val zoom = zoomProvider().coerceAtLeast(0.01f)

            // 1. Hover outline: dotted stroke (unscaled)
            if (hoveredBounds != null) {
                val strokePx = (1.5.dp.toPx() / zoom).coerceAtLeast(0.5f)
                val dashPx = (4.dp.toPx() / zoom)
                val gapPx = (3.dp.toPx() / zoom)
                drawRect(
                    color = FigmaBlue.copy(alpha = 0.85f),
                    topLeft = Offset(hoveredBounds.left, hoveredBounds.top),
                    size = Size(hoveredBounds.width, hoveredBounds.height),
                    style = Stroke(
                        width = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
                    )
                )
            }

            // 2. Selection outline: solid stroke (unscaled) + 4 corner handles
            for (id in targetSelectedIds) {
                val bounds = allBounds[id] ?: continue
                val strokePx = (2.dp.toPx() / zoom).coerceAtLeast(1f)
                drawRect(
                    color = FigmaBlue,
                    topLeft = Offset(bounds.left, bounds.top),
                    size = Size(bounds.width, bounds.height),
                    style = Stroke(width = strokePx)
                )

                // 4 Corner handles (8dp x 8dp white fill + 1.5dp blue border, unscaled)
                val handleSizePx = 8.dp.toPx() / zoom
                val halfHandle = handleSizePx / 2f
                val handleStrokePx = (1.5.dp.toPx() / zoom).coerceAtLeast(0.5f)

                val corners = listOf(
                    Offset(bounds.left, bounds.top),
                    Offset(bounds.right, bounds.top),
                    Offset(bounds.left, bounds.bottom),
                    Offset(bounds.right, bounds.bottom)
                )

                for (corner in corners) {
                    val handleTopLeft = Offset(corner.x - halfHandle, corner.y - halfHandle)
                    val handleSize = Size(handleSizePx, handleSizePx)

                    // Fill white
                    drawRect(
                        color = Color.White,
                        topLeft = handleTopLeft,
                        size = handleSize
                    )
                    // Border blue
                    drawRect(
                        color = FigmaBlue,
                        topLeft = handleTopLeft,
                        size = handleSize,
                        style = Stroke(width = handleStrokePx)
                    )
                }
            }

            // 3. Drag source visual (alpha 0.6 scrim)
            if (draggedNodeId != null) {
                val dragBounds = allBounds[draggedNodeId]
                if (dragBounds != null) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(dragBounds.left, dragBounds.top),
                        size = Size(dragBounds.width, dragBounds.height)
                    )
                }
            }

            // 4. Drop indicator line (2dp primary-colored line between siblings)
            if (dropTarget != null && dropTarget.isValid) {
                val strokePx = (2.dp.toPx() / zoom).coerceAtLeast(1.5f)
                drawLine(
                    color = FigmaBlue,
                    start = dropTarget.indicatorLineStart,
                    end = dropTarget.indicatorLineEnd,
                    strokeWidth = strokePx
                )
            }
        }

        // 3. Size and Component labels for selected node
        if (selectedBounds != null) {
            val widthDp = with(density) { selectedBounds.width.toDp().value.roundToInt() }
            val heightDp = with(density) { selectedBounds.height.toDp().value.roundToInt() }

            // Position size label: above top-left edge, or inside if within 24dp of top
            val isNearTop = selectedBounds.top < with(density) { 24.dp.toPx() }
            val labelOffsetY = if (isNearTop) {
                selectedBounds.top.roundToInt() + with(density) { 4.dp.toPx() }.roundToInt()
            } else {
                selectedBounds.top.roundToInt() - with(density) { 20.dp.toPx() }.roundToInt()
            }
            val labelOffsetX = selectedBounds.left.roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(labelOffsetX, labelOffsetY) }
                    .background(FigmaBlue, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "W $widthDp · H $heightDp",
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 10.sp
                )
            }

            // Component name label below bottom-left edge if selectedNode is known
            if (selectedNode != null) {
                val nameOffsetY = selectedBounds.bottom.roundToInt() + with(density) { 4.dp.toPx() }.roundToInt()
                Box(
                    modifier = Modifier
                        .offset { IntOffset(labelOffsetX, nameOffsetY) }
                        .background(FigmaBlue, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = selectedNode.label ?: selectedNode.kind.displayName,
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}
