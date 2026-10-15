package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ShellPanelsControllerTest {

    @Test
    fun testDragByClampsToMinMaxAndQuantizesTo1dp() {
        val slot = PanelSlotController(
            axis = PanelAxis.Width,
            minSizeDp = 200f,
            defaultSizeDp = 280f,
            initialSizeDp = 280f,
            initialCollapsed = false
        )
        slot.availableSpaceDp = 500f

        // 1dp quantization
        slot.dragBy(50.4f)
        assertEquals(330f, slot.sizeDp)

        // Clamping to available max
        slot.dragBy(300f)
        assertEquals(500f, slot.sizeDp)

        // Clamping to min
        slot.dragBy(-500f)
        assertEquals(200f, slot.sizeDp)

        // Negative delta at min is no-op (remains at min)
        slot.dragBy(-50f)
        assertEquals(200f, slot.sizeDp)
    }

    @Test
    fun testToggleTwiceRemembersExpandedSizeAndStaleBelowMinExpandsToDefault() = runTest {
        val slot = PanelSlotController(
            axis = PanelAxis.Width,
            minSizeDp = 200f,
            defaultSizeDp = 280f,
            initialSizeDp = 350f,
            initialCollapsed = false
        )

        // First toggle collapses
        slot.toggleAnimated()
        assertTrue(slot.collapsed)
        assertEquals(0f, slot.sizeDp)
        assertEquals(350f, slot.lastExpandedSizeDp)

        // Second toggle expands restoring 350f
        slot.toggleAnimated()
        assertFalse(slot.collapsed)
        assertEquals(350f, slot.sizeDp)

        // Stale lastExpandedSizeDp below min expands to defaultSizeDp
        slot.toggleAnimated()
        assertTrue(slot.collapsed)
        slot.lastExpandedSizeDp = 100f // Stale value < 200f min
        slot.toggleAnimated()
        assertFalse(slot.collapsed)
        assertEquals(280f, slot.sizeDp)
    }

    @Test
    fun testResetReturnsToDefaultAndClearsCollapsed() = runTest {
        val slot = PanelSlotController(
            axis = PanelAxis.Width,
            minSizeDp = 200f,
            defaultSizeDp = 280f,
            initialSizeDp = 420f,
            initialCollapsed = false
        )

        slot.resetAnimated()
        assertFalse(slot.collapsed)
        assertEquals(280f, slot.sizeDp)

        // Reset while collapsed uncollapses to default
        slot.collapseAnimated()
        assertTrue(slot.collapsed)
        slot.resetAnimated()
        assertFalse(slot.collapsed)
        assertEquals(280f, slot.sizeDp)
    }

    @Test
    fun testKeyboardAdjustQuantized8dpWithClamping() {
        val slot = PanelSlotController(
            axis = PanelAxis.Width,
            minSizeDp = 200f,
            defaultSizeDp = 280f,
            initialSizeDp = 280f,
            initialCollapsed = false
        )

        slot.keyboardAdjust(8f)
        assertEquals(288f, slot.sizeDp)

        slot.keyboardAdjust(-16f)
        assertEquals(272f, slot.sizeDp)

        slot.snapToMin()
        assertEquals(200f, slot.sizeDp)

        // Attempting to step below min clamps at min
        slot.keyboardAdjust(-8f)
        assertEquals(200f, slot.sizeDp)
    }

    @Test
    fun testUpdateAvailableSpaceShrinkAndCanvasRemainder() {
        val controller = ShellPanelsController()

        // Normal window dimensions
        controller.updateAvailableSpace(1200f, 800f)
        val initialMaxLeft = controller.left.availableSpaceDp

        // Grow window
        controller.updateAvailableSpace(1600f, 900f)
        assertTrue(controller.left.availableSpaceDp > initialMaxLeft)

        // Subsequent dragBy respects reduced max when window shrinks
        controller.updateAvailableSpace(800f, 600f)
        val reducedMaxLeft = controller.left.availableSpaceDp
        controller.left.dragBy(1000f)
        assertEquals(reducedMaxLeft, controller.left.sizeDp)

        // Severe window shrink below mins
        controller.updateAvailableSpace(200f, 200f)
        assertEquals(controller.left.minSizeDp, controller.left.availableSpaceDp)
        assertEquals(controller.right.minSizeDp, controller.right.availableSpaceDp)

        val canvasRemainder = controller.computeCanvasWidth(200f)
        assertTrue(canvasRemainder >= 0f)
    }

    @Test
    fun testDragByWhileCollapsedExpandsOutwardAndInwardIsNoOp() {
        var expandedTriggered = false
        val slot = PanelSlotController(
            axis = PanelAxis.Width,
            minSizeDp = 200f,
            defaultSizeDp = 280f,
            initialSizeDp = 0f,
            initialCollapsed = true
        )
        slot.onExpanded = { expandedTriggered = true }

        // Inward drag while collapsed is no-op
        slot.dragBy(-20f)
        assertTrue(slot.collapsed)
        assertEquals(0f, slot.sizeDp)
        assertFalse(expandedTriggered)

        // Outward drag while collapsed expands starting from minSizeDp + delta
        slot.dragBy(40f)
        assertFalse(slot.collapsed)
        assertEquals(240f, slot.sizeDp)
        assertTrue(expandedTriggered)
    }

    @Test
    fun testAnimationToggleAnimatedCompletesInRunTest() = runTest {
        val controller = ShellPanelsController()
        assertTrue(controller.isLeftOpen)
        assertEquals(280f, controller.left.sizeDp)

        controller.left.toggleAnimated()
        assertTrue(controller.left.collapsed)
        assertEquals(0f, controller.left.sizeDp)

        controller.left.toggleAnimated()
        assertFalse(controller.left.collapsed)
        assertEquals(280f, controller.left.sizeDp)
    }

    @Test
    fun testSelectRailTabAndFullWidthOrchestration() {
        val controller = ShellPanelsController()
        assertTrue(controller.isLeftOpen)
        assertTrue(controller.isRightOpen)
        assertFalse(controller.isFullWidth)

        // Selecting same active tab closes the left drawer
        controller.selectRailTab(StudioRailTab.Parts)
        assertFalse(controller.isLeftOpen)
        assertNull(controller.activeRailTab)

        // Selecting a new tab opens the left drawer with that tab
        controller.selectRailTab(StudioRailTab.Layers)
        assertTrue(controller.isLeftOpen)
        assertEquals(StudioRailTab.Layers, controller.activeRailTab)

        // Full width toggles both panels
        controller.toggleFullWidth()
        assertTrue(controller.isFullWidth)
        assertFalse(controller.isLeftOpen)
        assertFalse(controller.isRightOpen)

        // Exiting full width restores both
        controller.toggleFullWidth()
        assertFalse(controller.isFullWidth)
        assertTrue(controller.isLeftOpen)
        assertTrue(controller.isRightOpen)
        assertEquals(StudioRailTab.Layers, controller.activeRailTab)
    }
}
