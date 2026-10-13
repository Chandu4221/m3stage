package io.github.chandu4221.m3stage.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

enum class CanvasPointerTool {
    Select,
    Pan
}

/**
 * Ephemeral viewport transformation state for the infinite studio canvas.
 */
data class CanvasViewportState(
    val zoom: Float = 1.0f,
    val panOffset: Offset = Offset.Zero,
    val activeTool: CanvasPointerTool = CanvasPointerTool.Select,
    val viewportSize: Size = Size(1200f, 800f)
) {
    val zoomPercentage: Int
        get() = (zoom * 100).toInt()

    /**
     * Cursor-anchored zoom mutation.
     * Keeps the world/design point under [cursor] stationary on screen when scaling by [factor].
     */
    fun zoomAt(cursor: Offset, factor: Float): CanvasViewportState {
        val newZoom = (zoom * factor).coerceIn(0.25f, 2.5f)
        val effectiveFactor = newZoom / zoom
        val newPanOffset = cursor - (cursor - panOffset) * effectiveFactor
        return copy(zoom = newZoom, panOffset = newPanOffset)
    }

    /**
     * Center-anchored zoom mutation around [center].
     */
    fun zoomAtCenter(center: Offset, factor: Float): CanvasViewportState =
        zoomAt(center, factor)

    fun zoomIn(center: Offset? = null): CanvasViewportState {
        val anchor = center ?: if (viewportSize.width > 0f && viewportSize.height > 0f) {
            Offset(viewportSize.width / 2f, viewportSize.height / 2f)
        } else {
            Offset(600f, 400f)
        }
        val targetZoom = (zoom + 0.1f).coerceAtMost(2.5f)
        return if (targetZoom == zoom) this else zoomAt(anchor, targetZoom / zoom)
    }

    fun zoomOut(center: Offset? = null): CanvasViewportState {
        val anchor = center ?: if (viewportSize.width > 0f && viewportSize.height > 0f) {
            Offset(viewportSize.width / 2f, viewportSize.height / 2f)
        } else {
            Offset(600f, 400f)
        }
        val targetZoom = (zoom - 0.1f).coerceAtLeast(0.25f)
        return if (targetZoom == zoom) this else zoomAt(anchor, targetZoom / zoom)
    }

    fun zoomFit(): CanvasViewportState =
        copy(zoom = 1.0f, panOffset = Offset.Zero)

    fun panBy(delta: Offset): CanvasViewportState =
        copy(panOffset = panOffset + delta)

    fun tidy(): CanvasViewportState =
        copy(panOffset = Offset.Zero)
}