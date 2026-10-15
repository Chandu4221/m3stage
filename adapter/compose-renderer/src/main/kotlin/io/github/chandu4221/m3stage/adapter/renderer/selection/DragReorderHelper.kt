package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.github.chandu4221.m3stage.model.NodeId

data class DropTargetResult(
    val targetIndex: Int,
    val indicatorLineStart: Offset,
    val indicatorLineEnd: Offset,
    val isValid: Boolean
)

object DragReorderHelper {
    /**
     * Pure function computing the drop insertion index and visual drop-indicator line
     * given pointer coordinates relative to sibling layout bounds in artboard space.
     *
     * Invariants:
     * - Returns null if pointer is outside parentBounds (Constraint 4).
     * - Returns null if siblings list is empty or does not contain draggedNodeId.
     * - Reorders within the SAME parent only.
     */
    fun computeDropTarget(
        pointerPosition: Offset,
        draggedNodeId: NodeId,
        parentBounds: Rect?,
        siblings: List<Pair<NodeId, Rect>>,
        isVertical: Boolean
    ): DropTargetResult? {
        if (parentBounds == null || !parentBounds.contains(pointerPosition)) {
            return null
        }
        if (siblings.isEmpty()) {
            return null
        }

        val currentIndex = siblings.indexOfFirst { it.first == draggedNodeId }
        if (currentIndex == -1) return null

        val slot = if (isVertical) {
            val midpoints = siblings.map { (it.second.top + it.second.bottom) / 2f }
            val index = midpoints.indexOfFirst { pointerPosition.y < it }
            if (index == -1) siblings.size else index
        } else {
            val midpoints = siblings.map { (it.second.left + it.second.right) / 2f }
            val index = midpoints.indexOfFirst { pointerPosition.x < it }
            if (index == -1) siblings.size else index
        }

        val targetIndex = when {
            slot < currentIndex -> slot
            slot > currentIndex + 1 -> slot - 1
            else -> currentIndex
        }

        // Indicator line calculation (line between siblings)
        val minX = siblings.minOf { it.second.left }
        val maxX = siblings.maxOf { it.second.right }
        val minY = siblings.minOf { it.second.top }
        val maxY = siblings.maxOf { it.second.bottom }

        val lineStart: Offset
        val lineEnd: Offset

        if (isVertical) {
            val y = when (slot) {
                0 -> siblings[0].second.top
                siblings.size -> siblings.last().second.bottom
                else -> (siblings[slot - 1].second.bottom + siblings[slot].second.top) / 2f
            }
            lineStart = Offset(minX, y)
            lineEnd = Offset(maxX, y)
        } else {
            val x = when (slot) {
                0 -> siblings[0].second.left
                siblings.size -> siblings.last().second.right
                else -> (siblings[slot - 1].second.right + siblings[slot].second.left) / 2f
            }
            lineStart = Offset(x, minY)
            lineEnd = Offset(x, maxY)
        }

        return DropTargetResult(
            targetIndex = targetIndex,
            indicatorLineStart = lineStart,
            indicatorLineEnd = lineEnd,
            isValid = targetIndex != currentIndex
        )
    }
}
