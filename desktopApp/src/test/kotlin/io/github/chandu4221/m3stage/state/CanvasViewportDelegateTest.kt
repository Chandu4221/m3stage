package io.github.chandu4221.m3stage.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.math.abs

class CanvasViewportDelegateTest {

    private fun assertOffsetClose(expected: Offset, actual: Offset, tolerance: Float = 0.001f) {
        assertTrue(
            abs(expected.x - actual.x) <= tolerance && abs(expected.y - actual.y) <= tolerance,
            "Expected $expected but was $actual"
        )
    }

    @Test
    fun `zoomAt preserves world position under cursor`() {
        val initialPan = Offset(120f, 80f)
        val initialZoom = 1.0f
        val state = CanvasViewportState(zoom = initialZoom, panOffset = initialPan)

        val cursor = Offset(400f, 300f)
        val worldPointBefore = (cursor - initialPan) / initialZoom

        val factor = 1.25f
        val zoomed = state.zoomAt(cursor, factor)

        assertEquals(1.25f, zoomed.zoom, 0.001f)
        val screenPosAfter = zoomed.panOffset + (worldPointBefore * zoomed.zoom)
        assertOffsetClose(cursor, screenPosAfter)
    }

    @Test
    fun `zoomAt clamps zoom to valid range while preserving cursor invariance`() {
        val state = CanvasViewportState(zoom = 2.4f, panOffset = Offset(100f, 100f))
        val cursor = Offset(500f, 400f)
        val worldPointBefore = (cursor - state.panOffset) / state.zoom

        // Attempting to scale by 1.5 would give 3.6, which must clamp to 2.5
        val clamped = state.zoomAt(cursor, 1.5f)

        assertEquals(2.5f, clamped.zoom, 0.001f)
        val screenPosAfter = clamped.panOffset + (worldPointBefore * clamped.zoom)
        assertOffsetClose(cursor, screenPosAfter)
    }

    @Test
    fun `zoomOut clamps to min zoom 0_25`() {
        val state = CanvasViewportState(zoom = 0.3f, panOffset = Offset.Zero)
        val cursor = Offset(200f, 200f)
        val clamped = state.zoomAt(cursor, 0.5f)

        assertEquals(0.25f, clamped.zoom, 0.001f)
    }

    @Test
    fun `delegate handles pan and tool switching`() {
        val delegate = CanvasViewportDelegate()

        assertEquals(CanvasPointerTool.Select, delegate.viewportState.value.activeTool)
        delegate.setActiveTool(CanvasPointerTool.Pan)
        assertEquals(CanvasPointerTool.Pan, delegate.viewportState.value.activeTool)

        delegate.panBy(Offset(50f, -25f))
        assertEquals(Offset(50f, -25f), delegate.viewportState.value.panOffset)

        delegate.panBy(Offset(10f, 15f))
        assertEquals(Offset(60f, -10f), delegate.viewportState.value.panOffset)

        delegate.tidy()
        assertEquals(Offset.Zero, delegate.viewportState.value.panOffset)
    }

    @Test
    fun `delegate zoomFit centers content inside viewport`() {
        val delegate = CanvasViewportDelegate()
        val viewportSize = Size(1000f, 800f)
        val contentBounds = Rect(Offset(100f, 100f), Size(500f, 400f))

        delegate.zoomFit(viewportSize, contentBounds)

        // Scale: 1000/500 = 2.0, 800/400 = 2.0 -> zoom should be 2.0
        assertEquals(2.0f, delegate.viewportState.value.zoom, 0.001f)

        // Content center is (350, 300).
        // Transformed to screen: panOffset + (350, 300) * 2.0 must equal viewport center (500, 400)
        val screenCenter = delegate.viewportState.value.panOffset + (contentBounds.center * delegate.viewportState.value.zoom)
        assertOffsetClose(Offset(500f, 400f), screenCenter)
    }

    @Test
    fun `delegate tracks and updates individual screen offsets`() {
        val delegate = CanvasViewportDelegate()
        val screen1 = io.github.chandu4221.m3stage.model.ScreenId("screen-1")
        val screen2 = io.github.chandu4221.m3stage.model.ScreenId("screen-2")

        assertEquals(null, delegate.getScreenOffset(screen1))

        delegate.moveScreenBy(screen1, Offset(100f, 50f))
        assertEquals(Offset(100f, 50f), delegate.getScreenOffset(screen1))

        delegate.moveScreenBy(screen1, Offset(20f, -10f))
        assertEquals(Offset(120f, 40f), delegate.getScreenOffset(screen1))

        delegate.setScreenOffset(screen2, Offset(500f, 0f))
        assertEquals(Offset(500f, 0f), delegate.getScreenOffset(screen2))

        delegate.tidy()
        assertEquals(null, delegate.getScreenOffset(screen1))
        assertEquals(null, delegate.getScreenOffset(screen2))
    }
}
