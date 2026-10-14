package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab

/**
 * Immutable state representing a resizable, collapsible editor panel.
 *
 * @property targetWidth The user-selected width when expanded.
 * @property minWidth The minimum readable width constraint (floor).
 * @property defaultWidth The factory default width (restored on double-click).
 * @property isCollapsed Whether the panel is currently collapsed.
 * @property rememberedWidth The width saved prior to collapsing so re-expanding restores it.
 * @property collapseZoneOffset The margin below minWidth that must be crossed to trigger collapse.
 * @property isDragging Whether a splitter drag is actively in progress.
 * @property dragWidth The live tracking width during drag gestures.
 */
@Stable
data class PanelUiState(
    val targetWidth: Dp,
    val minWidth: Dp,
    val defaultWidth: Dp,
    val isCollapsed: Boolean = false,
    val rememberedWidth: Dp = targetWidth,
    val collapseZoneOffset: Dp = 24.dp,
    val isDragging: Boolean = false,
    val dragWidth: Dp = targetWidth
) {
    val collapseThreshold: Dp
        get() = (minWidth - collapseZoneOffset).coerceAtLeast(0.dp)

    val isGhostCollapsed: Boolean
        get() = isDragging && dragWidth < collapseThreshold

    fun resolvedWidth(maxWidth: Dp = Dp.Infinity): Dp {
        if (isCollapsed) return 0.dp
        if (isDragging) {
            return if (isGhostCollapsed) {
                0.dp
            } else {
                dragWidth.coerceIn(minWidth, maxWidth)
            }
        }
        return targetWidth.coerceIn(minWidth, maxWidth)
    }

    fun onDragStart(): PanelUiState {
        val initialWidth = if (isCollapsed) 0.dp else targetWidth
        return copy(
            isDragging = true,
            dragWidth = initialWidth
        )
    }

    fun onDragDelta(delta: Dp, maxWidth: Dp = Dp.Infinity): PanelUiState {
        val newDragWidth = (dragWidth + delta).coerceAtMost(maxWidth)
        return copy(dragWidth = newDragWidth)
    }

    fun onDragEnd(maxWidth: Dp = Dp.Infinity): PanelUiState {
        if (!isDragging) return this
        return if (dragWidth < collapseThreshold) {
            // Dragged past collapse zone -> Collapse and remember last good width
            val widthToRemember = if (targetWidth >= minWidth) targetWidth else defaultWidth
            copy(
                isDragging = false,
                isCollapsed = true,
                rememberedWidth = widthToRemember,
                dragWidth = 0.dp
            )
        } else if (dragWidth < minWidth) {
            // Dragged between collapse threshold and minWidth -> Snap to minWidth
            copy(
                isDragging = false,
                isCollapsed = false,
                targetWidth = minWidth,
                rememberedWidth = minWidth,
                dragWidth = minWidth
            )
        } else {
            // Valid width above minWidth
            val finalWidth = dragWidth.coerceIn(minWidth, maxWidth)
            copy(
                isDragging = false,
                isCollapsed = false,
                targetWidth = finalWidth,
                rememberedWidth = finalWidth,
                dragWidth = finalWidth
            )
        }
    }

    fun resetToDefault(): PanelUiState {
        return copy(
            isDragging = false,
            isCollapsed = false,
            targetWidth = defaultWidth,
            rememberedWidth = defaultWidth,
            dragWidth = defaultWidth
        )
    }

    fun toggleCollapsed(): PanelUiState {
        return if (isCollapsed) {
            expand()
        } else {
            collapse()
        }
    }

    fun collapse(): PanelUiState {
        val widthToRemember = if (targetWidth >= minWidth) targetWidth else defaultWidth
        return copy(
            isDragging = false,
            isCollapsed = true,
            rememberedWidth = widthToRemember
        )
    }

    fun expand(): PanelUiState {
        val restoreWidth = if (rememberedWidth >= minWidth) rememberedWidth else defaultWidth
        return copy(
            isDragging = false,
            isCollapsed = false,
            targetWidth = restoreWidth
        )
    }

    fun adjustWidthBy(delta: Dp, maxWidth: Dp = Dp.Infinity): PanelUiState {
        if (isCollapsed) {
            if (delta > 0.dp) {
                return expand()
            }
            return this
        }
        val newWidth = (targetWidth + delta).coerceIn(minWidth, maxWidth)
        return copy(
            targetWidth = newWidth,
            rememberedWidth = newWidth
        )
    }

    fun snapToMin(): PanelUiState {
        return copy(
            isDragging = false,
            isCollapsed = false,
            targetWidth = minWidth,
            rememberedWidth = minWidth
        )
    }

    val targetHeight: Dp get() = targetWidth
    val minHeight: Dp get() = minWidth
    val defaultHeight: Dp get() = defaultWidth
    val rememberedHeight: Dp get() = rememberedWidth
    val dragHeight: Dp get() = dragWidth
    fun resolvedHeight(maxHeight: Dp = Dp.Infinity): Dp = resolvedWidth(maxHeight)
}

/**
 * Manages the state of all resizable/collapsible editor panels.
 * Holds in-memory UI chrome state only; does not mutate project data or undo history.
 */
@Stable
class PanelStateHolder(
    initialLeft: PanelUiState = PanelUiState(
        targetWidth = 280.dp,
        minWidth = 200.dp,
        defaultWidth = 280.dp
    ),
    initialRight: PanelUiState = PanelUiState(
        targetWidth = 320.dp,
        minWidth = 240.dp,
        defaultWidth = 320.dp
    ),
    initialBottom: PanelUiState = PanelUiState(
        targetWidth = 280.dp,
        minWidth = 140.dp,
        defaultWidth = 280.dp,
        isCollapsed = true
    ),
    initialActiveRailTab: StudioRailTab? = StudioRailTab.Parts
) {
    var leftDrawer by mutableStateOf(initialLeft)
    var rightInspector by mutableStateOf(initialRight)
    var bottomCodePreview by mutableStateOf(initialBottom)
    var activeRailTab by mutableStateOf(initialActiveRailTab)
    var lastActiveRailTab by mutableStateOf(initialActiveRailTab ?: StudioRailTab.Parts)

    val isLeftOpen: Boolean
        get() = !leftDrawer.isCollapsed && activeRailTab != null

    val isRightOpen: Boolean
        get() = !rightInspector.isCollapsed

    val isBottomOpen: Boolean
        get() = !bottomCodePreview.isCollapsed

    val isFullWidth: Boolean
        get() = !isLeftOpen && !isRightOpen

    fun selectRailTab(tab: StudioRailTab) {
        if (activeRailTab == tab) {
            // Selecting current active tab collapses it
            activeRailTab = null
            leftDrawer = leftDrawer.collapse()
        } else {
            activeRailTab = tab
            lastActiveRailTab = tab
            leftDrawer = leftDrawer.expand()
        }
    }

    fun toggleLeftDrawer() {
        if (isLeftOpen) {
            activeRailTab = null
            leftDrawer = leftDrawer.collapse()
        } else {
            activeRailTab = lastActiveRailTab
            leftDrawer = leftDrawer.expand()
        }
    }

    fun toggleRightInspector() {
        rightInspector = rightInspector.toggleCollapsed()
    }

    fun toggleBottomCodePreview() {
        bottomCodePreview = bottomCodePreview.toggleCollapsed()
    }

    fun toggleFullWidth() {
        if (isFullWidth) {
            // Restore both panels
            activeRailTab = lastActiveRailTab
            leftDrawer = leftDrawer.expand()
            rightInspector = rightInspector.expand()
        } else {
            // Collapse both panels
            if (activeRailTab != null) {
                lastActiveRailTab = activeRailTab!!
            }
            activeRailTab = null
            leftDrawer = leftDrawer.collapse()
            rightInspector = rightInspector.collapse()
        }
    }
}

@Composable
fun rememberPanelStateHolder(): PanelStateHolder {
    return remember { PanelStateHolder() }
}
