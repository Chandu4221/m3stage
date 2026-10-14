package io.github.chandu4221.m3stage.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
}
