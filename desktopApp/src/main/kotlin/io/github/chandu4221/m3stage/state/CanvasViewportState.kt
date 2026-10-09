package io.github.chandu4221.m3stage.state

import androidx.compose.ui.geometry.Offset

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
    val activeTool: CanvasPointerTool = CanvasPointerTool.Select
) {
    val zoomPercentage: Int
        get() = (zoom * 100).toInt()

    fun zoomIn(): CanvasViewportState =
        copy(zoom = (zoom + 0.1f).coerceAtMost(2.5f))

    fun zoomOut(): CanvasViewportState =
        copy(zoom = (zoom - 0.1f).coerceAtLeast(0.25f))

    fun zoomFit(): CanvasViewportState =
        copy(zoom = 1.0f, panOffset = Offset.Zero)

    fun panBy(delta: Offset): CanvasViewportState =
        copy(panOffset = panOffset + delta)

    fun tidy(): CanvasViewportState =
        copy(panOffset = Offset.Zero)
}