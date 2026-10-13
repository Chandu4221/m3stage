package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

/**
 * Controller interface for viewport pan/zoom transformations and pointer tool mode.
 */
interface CanvasViewportController {
    val viewportState: State<CanvasViewportState>

    fun panBy(delta: Offset)
    fun zoomAt(cursor: Offset, factor: Float)
    fun zoomAtCenter(center: Offset, factor: Float)
    fun zoomIn(center: Offset? = null)
    fun zoomOut(center: Offset? = null)
    fun zoomFit(viewportSize: Size? = null, contentBounds: Rect? = null)
    fun resetZoom()
    fun tidy()
    fun setActiveTool(tool: CanvasPointerTool)
    fun setViewportSize(size: Size)
}

/**
 * Default implementation of [CanvasViewportController].
 * Uses [mutableStateOf] to support draw-phase state reads in graphicsLayer.
 */
class CanvasViewportDelegate(
    initialState: CanvasViewportState = CanvasViewportState()
) : CanvasViewportController {
    private val _viewportState = mutableStateOf(initialState)
    override val viewportState: State<CanvasViewportState> = _viewportState

    override fun panBy(delta: Offset) {
        _viewportState.value = _viewportState.value.panBy(delta)
    }

    override fun zoomAt(cursor: Offset, factor: Float) {
        _viewportState.value = _viewportState.value.zoomAt(cursor, factor)
    }

    override fun zoomAtCenter(center: Offset, factor: Float) {
        _viewportState.value = _viewportState.value.zoomAtCenter(center, factor)
    }

    override fun zoomIn(center: Offset?) {
        _viewportState.value = _viewportState.value.zoomIn(center)
    }

    override fun zoomOut(center: Offset?) {
        _viewportState.value = _viewportState.value.zoomOut(center)
    }

    override fun zoomFit(viewportSize: Size?, contentBounds: Rect?) {
        val targetSize = viewportSize ?: _viewportState.value.viewportSize
        if (viewportSize != null && viewportSize != _viewportState.value.viewportSize) {
            _viewportState.value = _viewportState.value.copy(viewportSize = viewportSize)
        }
        _viewportState.value = _viewportState.value.copy(viewportSize = targetSize).zoomFit(contentBounds)
    }

    override fun resetZoom() {
        _viewportState.value = _viewportState.value.resetZoom()
    }

    override fun tidy() {
        _viewportState.value = _viewportState.value.tidy()
    }

    override fun setActiveTool(tool: CanvasPointerTool) {
        _viewportState.value = _viewportState.value.copy(activeTool = tool)
    }

    override fun setViewportSize(size: Size) {
        if (_viewportState.value.viewportSize != size) {
            _viewportState.value = _viewportState.value.copy(viewportSize = size)
        }
    }
}

@Composable
fun rememberCanvasViewportController(
    initialState: CanvasViewportState = CanvasViewportState()
): CanvasViewportController = remember {
    CanvasViewportDelegate(initialState)
}
