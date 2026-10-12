package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import io.github.chandu4221.m3stage.model.NodeId

/**
 * Tracks the layout bounds and tree depth of all rendered DesignNodes in the preview canvas.
 * Enables hit-testing that always resolves to the deepest nested node under the pointer.
 */
class CanvasHitTracker {
    private val boundsMap = mutableMapOf<NodeId, Pair<Int, Rect>>()

    fun recordBounds(id: NodeId, depth: Int, coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        val pos = coordinates.positionInRoot()
        val size = coordinates.size
        boundsMap[id] = depth to Rect(
            left = pos.x,
            top = pos.y,
            right = pos.x + size.width,
            bottom = pos.y + size.height
        )
    }

    fun unregister(id: NodeId) {
        boundsMap.remove(id)
    }

    fun clear() {
        boundsMap.clear()
    }

    /**
     * Resolves the deepest node containing [rootPosition].
     * If multiple containers overlap, the one with the highest depth (the leaf child) is returned.
     */
    fun findDeepestNodeAt(rootPosition: Offset): NodeId? {
        if (!rootPosition.isSpecified) return null
        return boundsMap.entries
            .filter { (_, entry) -> entry.second.contains(rootPosition) }
            .maxByOrNull { (_, entry) -> entry.first }
            ?.key
    }
}
