package io.github.chandu4221.m3stage.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.state.PanelStateHolder
import io.github.chandu4221.m3stage.state.PanelUiState
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PanelLayoutTest {

    private val defaultLeftWidth: Dp = 280.dp
    private val minLeftWidth: Dp = 200.dp
    private val maxLeftWidth: Dp = 500.dp

    private val defaultRightWidth: Dp = 320.dp
    private val minRightWidth: Dp = 240.dp
    private val maxRightWidth: Dp = 600.dp

    @Test
    fun testLeftPanelClampingConstraints() {
        var leftWidth = defaultLeftWidth

        // Test normal delta expansion
        leftWidth = (leftWidth + 50.dp).coerceIn(minLeftWidth, maxLeftWidth)
        assertEquals(330.dp, leftWidth)

        // Test max constraint clamping
        leftWidth = (leftWidth + 400.dp).coerceIn(minLeftWidth, maxLeftWidth)
        assertEquals(maxLeftWidth, leftWidth)

        // Test min constraint clamping
        leftWidth = (leftWidth - 400.dp).coerceIn(minLeftWidth, maxLeftWidth)
        assertEquals(minLeftWidth, leftWidth)

        // Test double-click reset to default
        leftWidth = defaultLeftWidth
        assertEquals(defaultLeftWidth, leftWidth)
    }

    @Test
    fun testRightPanelClampingConstraints() {
        var rightWidth = defaultRightWidth

        // Test normal delta expansion (dragging left expands right panel)
        rightWidth = (rightWidth + 60.dp).coerceIn(minRightWidth, maxRightWidth)
        assertEquals(380.dp, rightWidth)

        // Test max constraint clamping
        rightWidth = (rightWidth + 500.dp).coerceIn(minRightWidth, maxRightWidth)
        assertEquals(maxRightWidth, rightWidth)

        // Test min constraint clamping
        rightWidth = (rightWidth - 500.dp).coerceIn(minRightWidth, maxRightWidth)
        assertEquals(minRightWidth, rightWidth)

        // Test reset to default
        rightWidth = defaultRightWidth
        assertEquals(defaultRightWidth, rightWidth)
    }

    @Test
    fun testFullWidthToggleTransitions() {
        var activeRailTab: StudioRailTab? = StudioRailTab.Parts
        var lastActiveRailTab = StudioRailTab.Parts
        var isRightPanelOpen = true

        val isLeftOpen = activeRailTab != null
        var isFullWidth = !isLeftOpen && !isRightPanelOpen
        assertFalse(isFullWidth)

        // Toggle Full Width -> Both panels collapse
        if (isFullWidth) {
            activeRailTab = lastActiveRailTab
            isRightPanelOpen = true
        } else {
            activeRailTab = null
            isRightPanelOpen = false
        }
        isFullWidth = activeRailTab == null && !isRightPanelOpen
        assertTrue(isFullWidth)
        assertNull(activeRailTab)
        assertFalse(isRightPanelOpen)

        // Toggle Full Width again -> Both panels restored
        if (isFullWidth) {
            activeRailTab = lastActiveRailTab
            isRightPanelOpen = true
        } else {
            activeRailTab = null
            isRightPanelOpen = false
        }
        isFullWidth = activeRailTab == null && !isRightPanelOpen
        assertFalse(isFullWidth)
        assertEquals(StudioRailTab.Parts, activeRailTab)
        assertTrue(isRightPanelOpen)
    }

    @Test
    fun testRailTabSelectionTogglesPanel() {
        var activeRailTab: StudioRailTab? = StudioRailTab.Parts
        var lastActiveRailTab = StudioRailTab.Parts

        // Selecting the currently active tab should collapse it
        fun onTabSelected(tab: StudioRailTab) {
            if (activeRailTab == tab) {
                activeRailTab = null
            } else {
                activeRailTab = tab
                lastActiveRailTab = tab
            }
        }

        onTabSelected(StudioRailTab.Parts)
        assertNull(activeRailTab)
        assertEquals(StudioRailTab.Parts, lastActiveRailTab)

        // Selecting a tab while collapsed opens it
        onTabSelected(StudioRailTab.Layers)
        assertEquals(StudioRailTab.Layers, activeRailTab)
        assertEquals(StudioRailTab.Layers, lastActiveRailTab)

        // Selecting a different tab switches to it
        onTabSelected(StudioRailTab.Theme)
        assertEquals(StudioRailTab.Theme, activeRailTab)
        assertEquals(StudioRailTab.Theme, lastActiveRailTab)
    }

    @Test
    fun testPanelUiStateDragAndSnapBack() {
        var state = PanelUiState(
            targetWidth = 280.dp,
            minWidth = 200.dp,
            defaultWidth = 280.dp,
            collapseZoneOffset = 24.dp
        )
        assertEquals(176.dp, state.collapseThreshold)

        // Start drag
        state = state.onDragStart()
        assertTrue(state.isDragging)
        assertEquals(280.dp, state.dragWidth)

        // Drag delta within normal readable range
        state = state.onDragDelta((-50).dp)
        assertEquals(230.dp, state.dragWidth)
        assertEquals(230.dp, state.resolvedWidth())
        assertFalse(state.isGhostCollapsed)

        // Drag delta into the resistance zone (between 176dp and 200dp)
        state = state.onDragDelta((-45).dp) // dragWidth = 185dp
        assertEquals(185.dp, state.dragWidth)
        assertEquals(200.dp, state.resolvedWidth()) // Clamped visually to minWidth
        assertFalse(state.isGhostCollapsed)

        // Release inside the resistance zone -> snaps back to minWidth
        state = state.onDragEnd()
        assertFalse(state.isDragging)
        assertFalse(state.isCollapsed)
        assertEquals(200.dp, state.targetWidth)
        assertEquals(200.dp, state.resolvedWidth())
    }

    @Test
    fun testPanelUiStateDragIntoCollapseZoneAndRestore() {
        var state = PanelUiState(
            targetWidth = 350.dp,
            minWidth = 200.dp,
            defaultWidth = 280.dp,
            collapseZoneOffset = 24.dp
        )

        // Start drag and pull past the 176dp collapse threshold
        state = state.onDragStart()
        state = state.onDragDelta((-200).dp) // dragWidth = 150dp (< 176dp)
        assertEquals(150.dp, state.dragWidth)
        assertTrue(state.isGhostCollapsed)
        assertEquals(0.dp, state.resolvedWidth())

        // Release inside collapse zone -> collapses and remembers 350dp
        state = state.onDragEnd()
        assertTrue(state.isCollapsed)
        assertEquals(350.dp, state.rememberedWidth)
        assertEquals(0.dp, state.resolvedWidth())

        // Expand restores remembered 350dp
        state = state.expand()
        assertFalse(state.isCollapsed)
        assertEquals(350.dp, state.targetWidth)
        assertEquals(350.dp, state.resolvedWidth())
    }

    @Test
    fun testPanelUiStateDoubleClickReset() {
        var state = PanelUiState(
            targetWidth = 450.dp,
            minWidth = 200.dp,
            defaultWidth = 280.dp
        )

        state = state.resetToDefault()
        assertEquals(280.dp, state.targetWidth)
        assertEquals(280.dp, state.resolvedWidth())
        assertFalse(state.isCollapsed)

        // Double click while collapsed uncollapses to default width
        state = state.collapse()
        assertTrue(state.isCollapsed)
        state = state.resetToDefault()
        assertFalse(state.isCollapsed)
        assertEquals(280.dp, state.targetWidth)
    }

    @Test
    fun testPanelUiStateKeyboardAccessibility() {
        var state = PanelUiState(
            targetWidth = 280.dp,
            minWidth = 200.dp,
            defaultWidth = 280.dp
        )

        // Arrow keys step by 8dp
        state = state.adjustWidthBy(8.dp)
        assertEquals(288.dp, state.targetWidth)

        state = state.adjustWidthBy((-16).dp)
        assertEquals(272.dp, state.targetWidth)

        // Snap to min
        state = state.snapToMin()
        assertEquals(200.dp, state.targetWidth)

        // Attempting to step below min clamps at min
        state = state.adjustWidthBy((-8).dp)
        assertEquals(200.dp, state.targetWidth)
    }

    @Test
    fun testPanelStateHolderOrchestration() {
        val holder = PanelStateHolder()
        assertTrue(holder.isLeftOpen)
        assertTrue(holder.isRightOpen)
        assertFalse(holder.isFullWidth)

        // Toggle Full Width collapses both
        holder.toggleFullWidth()
        assertTrue(holder.isFullWidth)
        assertFalse(holder.isLeftOpen)
        assertFalse(holder.isRightOpen)

        // Toggle Full Width again restores both
        holder.toggleFullWidth()
        assertFalse(holder.isFullWidth)
        assertTrue(holder.isLeftOpen)
        assertTrue(holder.isRightOpen)

        // Select rail tab to collapse left drawer
        holder.selectRailTab(StudioRailTab.Parts)
        assertFalse(holder.isLeftOpen)

        // Select different tab restores drawer with that tab
        holder.selectRailTab(StudioRailTab.Layers)
        assertTrue(holder.isLeftOpen)
        assertEquals(StudioRailTab.Layers, holder.activeRailTab)
    }
}
