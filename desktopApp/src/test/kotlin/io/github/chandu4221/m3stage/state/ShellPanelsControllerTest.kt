package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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

    @Test
    fun testT1aLeftCollapseAndExpandAnimateThroughIntermediateValues() = runTest {
        val clock = VirtualTimeFrameClock(testScheduler)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + clock)
        val controller = ShellPanelsController(coroutineScope = scope)

        assertEquals(280f, controller.left.sizeDp)
        assertFalse(controller.left.collapsed)

        // Collapse
        controller.toggleLeft()
        val collapseIntermediateValues = mutableListOf<Float>()
        while (!controller.left.collapsed) {
            testScheduler.advanceTimeBy(16)
            if (controller.left.sizeDp > 0f && controller.left.sizeDp < 280f) {
                if (collapseIntermediateValues.isEmpty() || collapseIntermediateValues.last() != controller.left.sizeDp) {
                    collapseIntermediateValues.add(controller.left.sizeDp)
                }
            }
        }
        assertEquals(0f, controller.left.sizeDp)
        assertTrue(controller.left.collapsed)
        assertTrue(
            collapseIntermediateValues.size >= 3,
            "Collapse must pass through >= 3 intermediate values, but had: $collapseIntermediateValues"
        )

        // Expand
        controller.toggleLeft()
        val expandIntermediateValues = mutableListOf<Float>()
        while (controller.left.sizeDp < 280f) {
            testScheduler.advanceTimeBy(16)
            if (controller.left.sizeDp > 0f && controller.left.sizeDp < 280f) {
                if (expandIntermediateValues.isEmpty() || expandIntermediateValues.last() != controller.left.sizeDp) {
                    expandIntermediateValues.add(controller.left.sizeDp)
                }
            }
        }
        assertEquals(280f, controller.left.sizeDp)
        assertFalse(controller.left.collapsed)
        assertTrue(
            expandIntermediateValues.size >= 3,
            "Expand must pass through >= 3 intermediate values, but had: $expandIntermediateValues"
        )
    }

    @Test
    fun testT1bTabClearedAtCompletionNotAtStart() = runTest {
        val clock = VirtualTimeFrameClock(testScheduler)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + clock)
        val controller = ShellPanelsController(coroutineScope = scope)

        assertEquals(StudioRailTab.Parts, controller.activeRailTab)
        controller.toggleLeft()

        // Tab MUST still be non-null immediately after toggleLeft() initiates collapse
        assertNotNull(controller.activeRailTab)
        assertEquals(StudioRailTab.Parts, controller.activeRailTab)

        // Advance mid-flight
        testScheduler.advanceTimeBy(48)
        assertNotNull(controller.activeRailTab)
        assertEquals(StudioRailTab.Parts, controller.activeRailTab)
        assertTrue(controller.left.sizeDp > 0f)

        // Complete collapse
        testScheduler.advanceUntilIdle()
        assertEquals(0f, controller.left.sizeDp)
        assertTrue(controller.left.collapsed)
        assertNull(controller.activeRailTab)
    }

    @Test
    fun testT1cRaceRetoggleMidCollapseInterruptsAndPreservesTab() = runTest {
        val clock = VirtualTimeFrameClock(testScheduler)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + clock)
        val controller = ShellPanelsController(coroutineScope = scope)

        assertEquals(StudioRailTab.Parts, controller.activeRailTab)
        controller.toggleLeft()

        // Advance 50ms into collapse
        testScheduler.advanceTimeBy(50)
        assertTrue(controller.left.sizeDp in 1f..279f, "Expected mid-flight width, got: ${controller.left.sizeDp}")
        assertNotNull(controller.activeRailTab)

        // Re-toggle mid-flight (must expand)
        controller.toggleLeft()
        assertEquals(StudioRailTab.Parts, controller.activeRailTab)

        // Advance to full completion
        testScheduler.advanceUntilIdle()
        assertFalse(controller.left.collapsed)
        assertEquals(280f, controller.left.sizeDp)
        assertEquals(StudioRailTab.Parts, controller.activeRailTab)
    }

    @Test
    fun testT1cSelectRailTabMidCollapseSwitchesAndPreservesTab() = runTest {
        val clock = VirtualTimeFrameClock(testScheduler)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + clock)
        val controller = ShellPanelsController(coroutineScope = scope)

        assertEquals(StudioRailTab.Parts, controller.activeRailTab)
        controller.toggleLeft()

        // Advance 50ms into collapse
        testScheduler.advanceTimeBy(50)
        assertTrue(controller.left.sizeDp in 1f..279f)

        // Select Layers tab mid-collapse
        controller.selectRailTab(StudioRailTab.Layers)
        assertEquals(StudioRailTab.Layers, controller.activeRailTab)

        // Advance to full completion
        testScheduler.advanceUntilIdle()
        assertFalse(controller.left.collapsed)
        assertEquals(280f, controller.left.sizeDp)
        assertEquals(StudioRailTab.Layers, controller.activeRailTab)
    }

    @Test
    fun testT2aSpringTerminatesByThresholdNoCrawl() = runTest {
        val clock = VirtualTimeFrameClock(testScheduler)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + clock)
        val controller = ShellPanelsController(coroutineScope = scope)

        val right = controller.right
        assertEquals(320f, right.sizeDp)

        right.collapse()
        var elapsedMs = 0L
        var timeEnteredFinal10dp = -1L

        while (!right.collapsed && elapsedMs < 1000L) {
            testScheduler.advanceTimeBy(16)
            elapsedMs += 16
            if (right.sizeDp < 10f && timeEnteredFinal10dp < 0L) {
                timeEnteredFinal10dp = elapsedMs
            }
        }

        assertEquals(0f, right.sizeDp)
        assertTrue(right.collapsed)
        // Bounded total duration: <= 300ms virtual
        assertTrue(elapsedMs <= 300L, "Collapse took $elapsedMs ms, expected <= 300ms")
        // No lingering in final 10dp: duration from < 10dp to 0dp <= 60ms virtual
        assertNotEquals(-1L, timeEnteredFinal10dp)
        val final10dpDuration = elapsedMs - timeEnteredFinal10dp
        assertTrue(
            final10dpDuration <= 60L,
            "Lingered in final 10dp for $final10dpDuration ms, expected <= 60ms"
        )
    }

    @Test
    fun testT2bAllThreePanelsSymmetricAnimationHygiene() = runTest {
        val clock = VirtualTimeFrameClock(testScheduler)
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler) + clock)
        val controller = ShellPanelsController(coroutineScope = scope)

        val slots = listOf("left" to controller.left, "right" to controller.right, "bottom" to controller.bottom)

        for ((name, slot) in slots) {
            // Ensure slot is expanded
            if (slot.collapsed) {
                slot.expand()
                testScheduler.advanceUntilIdle()
            }
            val startSize = slot.sizeDp
            assertTrue(startSize > 0f)

            // Collapse
            slot.collapse()
            var elapsedMs = 0L
            var timeEnteredFinal10dp = -1L
            val intermediates = mutableListOf<Float>()

            while (!slot.collapsed && elapsedMs < 1000L) {
                testScheduler.advanceTimeBy(16)
                elapsedMs += 16
                if (slot.sizeDp > 0f && slot.sizeDp < startSize) {
                    if (intermediates.isEmpty() || intermediates.last() != slot.sizeDp) {
                        intermediates.add(slot.sizeDp)
                    }
                }
                if (slot.sizeDp < 10f && timeEnteredFinal10dp < 0L) {
                    timeEnteredFinal10dp = elapsedMs
                }
            }

            assertEquals(0f, slot.sizeDp, "$name sizeDp should be 0f")
            assertTrue(slot.collapsed, "$name should be collapsed")
            assertTrue(intermediates.size >= 3, "$name must pass through >= 3 intermediates, got: $intermediates")
            assertTrue(elapsedMs <= 300L, "$name collapse took $elapsedMs ms, expected <= 300ms")
            assertNotEquals(-1L, timeEnteredFinal10dp, "$name should have traversed final 10dp")
            val final10dpDuration = elapsedMs - timeEnteredFinal10dp
            assertTrue(final10dpDuration <= 60L, "$name lingered in final 10dp for $final10dpDuration ms, expected <= 60ms")
        }
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
private class VirtualTimeFrameClock(
    private val scheduler: kotlinx.coroutines.test.TestCoroutineScheduler,
    private val frameDurationMs: Long = 16L
) : androidx.compose.runtime.MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        kotlinx.coroutines.delay(frameDurationMs)
        return onFrame(scheduler.currentTime * 1_000_000L)
    }
}
