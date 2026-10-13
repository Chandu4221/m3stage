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
    val screenOffsets: State<Map<io.github.chandu4221.m3stage.model.ScreenId, Offset>>

    fun panBy(delta: Offset)
    fun zoomAt(cursor: Offset, factor: Float)
    fun zoomAtCenter(center: Offset, factor: Float)
    fun zoomIn(center: Offset? = null)
    fun zoomOut(center: Offset? = null)
    fun zoomFit(viewportSize: Size? = null, contentBounds: Rect? = null)
    fun resetZoom()
    fun tidy()
    fun setActiveTool(tool: CanvasPointerTool)

    fun getScreenOffset(screenId: io.github.chandu4221.m3stage.model.ScreenId): Offset?
    fun setScreenOffset(screenId: io.github.chandu4221.m3stage.model.ScreenId, offset: Offset)
    fun moveScreenBy(screenId: io.github.chandu4221.m3stage.model.ScreenId, delta: Offset)
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

    private val _screenOffsets = mutableStateOf<Map<io.github.chandu4221.m3stage.model.ScreenId, Offset>>(emptyMap())
    override val screenOffsets: State<Map<io.github.chandu4221.m3stage.model.ScreenId, Offset>> = _screenOffsets

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
        if (viewportSize != null && contentBounds != null && contentBounds.width > 0f && contentBounds.height > 0f) {
            val scaleX = viewportSize.width / contentBounds.width
            val scaleY = viewportSize.height / contentBounds.height
            val fitZoom = minOf(scaleX, scaleY).coerceIn(0.25f, 2.5f)
            val centerViewport = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
            val centerContent = contentBounds.center
            val newPan = centerViewport - (centerContent * fitZoom)
            _viewportState.value = _viewportState.value.copy(zoom = fitZoom, panOffset = newPan)
        } else {
            _viewportState.value = _viewportState.value.zoomFit()
        }
    }

    override fun resetZoom() {
        _viewportState.value = _viewportState.value.copy(zoom = 1.0f, panOffset = Offset.Zero)
    }

    override fun tidy() {
        _viewportState.value = _viewportState.value.tidy()
        _screenOffsets.value = emptyMap()
    }

    override fun setActiveTool(tool: CanvasPointerTool) {
        _viewportState.value = _viewportState.value.copy(activeTool = tool)
    }

    override fun getScreenOffset(screenId: io.github.chandu4221.m3stage.model.ScreenId): Offset? =
        _screenOffsets.value[screenId]

    override fun setScreenOffset(screenId: io.github.chandu4221.m3stage.model.ScreenId, offset: Offset) {
        val current = _screenOffsets.value.toMutableMap()
        current[screenId] = offset
        _screenOffsets.value = current
    }

    override fun moveScreenBy(screenId: io.github.chandu4221.m3stage.model.ScreenId, delta: Offset) {
        val current = _screenOffsets.value.toMutableMap()
        val old = current[screenId] ?: Offset.Zero
        current[screenId] = old + delta
        _screenOffsets.value = current
    }
}

@Composable
fun rememberCanvasViewportController(
    initialState: CanvasViewportState = CanvasViewportState()
): CanvasViewportController = remember {
    CanvasViewportDelegate(initialState)
}
