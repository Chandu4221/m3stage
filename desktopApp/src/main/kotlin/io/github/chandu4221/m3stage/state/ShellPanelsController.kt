package io.github.chandu4221.m3stage.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.organism.StudioRailTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PanelAxis {
    Width,
    Height
}

/**
 * Controller managing the state, dragging, animation, and constraints of an individual editor panel.
 *
 * @param axis The primary dimension axis (Width for side panels, Height for bottom panel).
 * @param minSizeDp Hard floor below which the panel cannot be dragged.
 * @param defaultSizeDp Factory default size restored on reset (e.g. double-click).
 * @param initialSizeDp Starting size.
 * @param initialCollapsed Whether the panel starts collapsed.
 * @param coroutineScope Scope used for launching spring animations on UI actions.
 */
@Stable
class PanelSlotController(
    val axis: PanelAxis,
    val minSizeDp: Float,
    val defaultSizeDp: Float,
    initialSizeDp: Float = defaultSizeDp,
    initialCollapsed: Boolean = false,
    private val coroutineScope: CoroutineScope? = null,
    var onExpanded: (() -> Unit)? = null,
    var onCollapsed: (() -> Unit)? = null
) {
    companion object {
        const val COLLAPSE_STIFFNESS = 3800f
        const val EXPAND_STIFFNESS = Spring.StiffnessMediumLow
        const val VISIBILITY_THRESHOLD = 0.5f
    }

    var sizeDp by mutableStateOf(if (initialCollapsed) 0f else initialSizeDp)
        private set

    var collapsed by mutableStateOf(initialCollapsed)
        private set

    var targetCollapsed: Boolean by mutableStateOf(initialCollapsed)
        private set

    var lastExpandedSizeDp: Float = initialSizeDp
        internal set

    var isDragging by mutableStateOf(false)
        private set

    // Plain field for clamping bounds; updated without triggering recomposition
    var availableSpaceDp: Float = Float.MAX_VALUE

    val size: Dp
        get() = sizeDp.dp

    private val animatable = Animatable(if (initialCollapsed) 0f else initialSizeDp, visibilityThreshold = VISIBILITY_THRESHOLD)
    private var animJob: kotlinx.coroutines.Job? = null
    private var animToken: Long = 0L

    /**
     * Updates panel size during mouse drag. Direct, quantized, and strictly un-animated.
     */
    fun dragBy(deltaDp: Float) {
        animJob?.cancel()
        isDragging = true
        if (collapsed) {
            // Dragging outward from collapsed expands immediately from minSizeDp
            if (deltaDp > 0f) {
                collapsed = false
                targetCollapsed = false
                val target = (minSizeDp + deltaDp).coerceIn(minSizeDp, availableSpaceDp).roundToInt().toFloat()
                sizeDp = target
                lastExpandedSizeDp = target
                onExpanded?.invoke()
            }
            return
        }
        val target = (sizeDp + deltaDp).coerceIn(minSizeDp, availableSpaceDp).roundToInt().toFloat()
        sizeDp = target
        lastExpandedSizeDp = target
    }

    /**
     * Marks drag completion, snap-completing any animation state.
     */
    fun endDrag() {
        isDragging = false
    }

    /**
     * Toggles collapsed/expanded state with spring animation.
     */
    suspend fun toggleAnimated() {
        if (targetCollapsed) {
            expandAnimated()
        } else {
            collapseAnimated()
        }
    }

    suspend fun expandAnimated() {
        targetCollapsed = false
        val token = ++animToken
        collapsed = false
        onExpanded?.invoke()
        val target = if (lastExpandedSizeDp < minSizeDp) defaultSizeDp else lastExpandedSizeDp.coerceIn(minSizeDp, availableSpaceDp)
        val hasClock = kotlin.coroutines.coroutineContext[androidx.compose.runtime.MonotonicFrameClock] != null
        if (hasClock) {
            animatable.snapTo(sizeDp)
            animatable.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = EXPAND_STIFFNESS,
                    visibilityThreshold = VISIBILITY_THRESHOLD
                )
            ) {
                sizeDp = value
            }
        } else {
            animatable.snapTo(target)
        }
        if (token == animToken) {
            sizeDp = target
            lastExpandedSizeDp = target
        }
    }

    suspend fun collapseAnimated() {
        targetCollapsed = true
        val token = ++animToken
        lastExpandedSizeDp = if (sizeDp >= minSizeDp) sizeDp else defaultSizeDp
        val hasClock = kotlin.coroutines.coroutineContext[androidx.compose.runtime.MonotonicFrameClock] != null
        if (hasClock) {
            animatable.snapTo(sizeDp)
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = COLLAPSE_STIFFNESS,
                    visibilityThreshold = VISIBILITY_THRESHOLD
                )
            ) {
                sizeDp = value
            }
        } else {
            animatable.snapTo(0f)
        }
        if (token == animToken) {
            sizeDp = 0f
            collapsed = true
            onCollapsed?.invoke()
        }
    }

    fun toggle() {
        if (targetCollapsed) expand() else collapse()
    }

    fun expand() {
        targetCollapsed = false
        val scope = coroutineScope
        if (scope != null) {
            animJob?.cancel()
            animJob = scope.launch { expandAnimated() }
        } else {
            animJob?.cancel()
            ++animToken
            collapsed = false
            onExpanded?.invoke()
            val target = if (lastExpandedSizeDp < minSizeDp) defaultSizeDp else lastExpandedSizeDp.coerceIn(minSizeDp, availableSpaceDp)
            sizeDp = target
            lastExpandedSizeDp = target
        }
    }

    fun collapse() {
        targetCollapsed = true
        val scope = coroutineScope
        if (scope != null) {
            animJob?.cancel()
            animJob = scope.launch { collapseAnimated() }
        } else {
            animJob?.cancel()
            ++animToken
            lastExpandedSizeDp = if (sizeDp >= minSizeDp) sizeDp else defaultSizeDp
            sizeDp = 0f
            collapsed = true
            onCollapsed?.invoke()
        }
    }

    /**
     * Double-click on handle resets to default size and uncollapses.
     */
    suspend fun resetAnimated() {
        targetCollapsed = false
        val token = ++animToken
        collapsed = false
        onExpanded?.invoke()
        val target = defaultSizeDp.coerceIn(minSizeDp, availableSpaceDp)
        val hasClock = kotlin.coroutines.coroutineContext[androidx.compose.runtime.MonotonicFrameClock] != null
        if (hasClock) {
            animatable.snapTo(sizeDp)
            animatable.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = EXPAND_STIFFNESS,
                    visibilityThreshold = VISIBILITY_THRESHOLD
                )
            ) {
                sizeDp = value
            }
        } else {
            animatable.snapTo(target)
        }
        if (token == animToken) {
            sizeDp = target
            lastExpandedSizeDp = target
        }
    }

    fun reset() {
        targetCollapsed = false
        val scope = coroutineScope
        if (scope != null) {
            animJob?.cancel()
            animJob = scope.launch { resetAnimated() }
        } else {
            animJob?.cancel()
            ++animToken
            collapsed = false
            onExpanded?.invoke()
            val target = defaultSizeDp.coerceIn(minSizeDp, availableSpaceDp)
            sizeDp = target
            lastExpandedSizeDp = target
        }
    }

    /**
     * Keyboard adjustment by delta (e.g. ±8dp). Instant quantization and clamping, no animation.
     */
    fun keyboardAdjust(deltaDp: Float) {
        animJob?.cancel()
        if (collapsed) {
            if (deltaDp > 0f) {
                collapsed = false
                targetCollapsed = false
                val target = minSizeDp.coerceIn(minSizeDp, availableSpaceDp).roundToInt().toFloat()
                sizeDp = target
                lastExpandedSizeDp = target
                onExpanded?.invoke()
            }
            return
        }
        val target = (sizeDp + deltaDp).coerceIn(minSizeDp, availableSpaceDp).roundToInt().toFloat()
        sizeDp = target
        lastExpandedSizeDp = target
    }

    /**
     * Ctrl+arrow or End key: snaps instantly to min size.
     */
    fun snapToMin() {
        animJob?.cancel()
        collapsed = false
        targetCollapsed = false
        val target = minSizeDp.coerceIn(minSizeDp, availableSpaceDp)
        sizeDp = target
        lastExpandedSizeDp = target
        onExpanded?.invoke()
    }
}

/**
 * Orchestrates all panels (left drawer, right inspector, bottom code preview) in the editor shell.
 * Keeps panel sizes isolated from top-level screen composition.
 */
@Stable
class ShellPanelsController(
    coroutineScope: CoroutineScope? = null,
    initialActiveRailTab: StudioRailTab? = StudioRailTab.Parts
) {
    companion object {
        const val LEFT_DEFAULT_DP = 280f
        const val LEFT_MIN_DP = 200f

        const val RIGHT_DEFAULT_DP = 320f
        const val RIGHT_MIN_DP = 240f

        const val BOTTOM_DEFAULT_DP = 280f
        const val BOTTOM_MIN_DP = 140f

        const val MIN_CANVAS_DP = 240f
        const val NAV_RAIL_WIDTH_DP = 72f
    }

    val left = PanelSlotController(
        axis = PanelAxis.Width,
        minSizeDp = LEFT_MIN_DP,
        defaultSizeDp = LEFT_DEFAULT_DP,
        initialSizeDp = LEFT_DEFAULT_DP,
        initialCollapsed = false,
        coroutineScope = coroutineScope
    )

    val right = PanelSlotController(
        axis = PanelAxis.Width,
        minSizeDp = RIGHT_MIN_DP,
        defaultSizeDp = RIGHT_DEFAULT_DP,
        initialSizeDp = RIGHT_DEFAULT_DP,
        initialCollapsed = false,
        coroutineScope = coroutineScope
    )

    val bottom = PanelSlotController(
        axis = PanelAxis.Height,
        minSizeDp = BOTTOM_MIN_DP,
        defaultSizeDp = BOTTOM_DEFAULT_DP,
        initialSizeDp = BOTTOM_DEFAULT_DP,
        initialCollapsed = true,
        coroutineScope = coroutineScope
    )

    var activeRailTab by mutableStateOf(initialActiveRailTab)
    var lastActiveRailTab by mutableStateOf(initialActiveRailTab ?: StudioRailTab.Parts)

    init {
        left.onExpanded = {
            if (activeRailTab == null) {
                activeRailTab = lastActiveRailTab
            }
        }
        left.onCollapsed = {
            activeRailTab = null
        }
    }

    val isLeftOpen: Boolean
        get() = !left.targetCollapsed && activeRailTab != null

    val isRightOpen: Boolean
        get() = !right.targetCollapsed

    val isBottomOpen: Boolean
        get() = !bottom.targetCollapsed

    val isFullWidth: Boolean
        get() = !isLeftOpen && !isRightOpen

    fun selectRailTab(tab: StudioRailTab) {
        if (activeRailTab == tab && !left.targetCollapsed) {
            // Re-clicking active tab collapses the left drawer
            left.collapse()
        } else {
            activeRailTab = tab
            lastActiveRailTab = tab
            left.expand()
        }
    }

    fun toggleLeft() {
        if (isLeftOpen) {
            left.collapse()
        } else {
            activeRailTab = lastActiveRailTab
            left.expand()
        }
    }

    fun toggleRight() {
        right.toggle()
    }

    fun toggleBottom() {
        bottom.toggle()
    }

    fun toggleFullWidth() {
        if (isFullWidth) {
            // Restore both panels
            activeRailTab = lastActiveRailTab
            left.expand()
            right.expand()
        } else {
            // Collapse both panels
            if (activeRailTab != null) {
                lastActiveRailTab = activeRailTab!!
            }
            left.collapse()
            right.collapse()
        }
    }

    /**
     * Updates clamping bounds on slots from window dimensions.
     * Plain field writes only; zero recompositions.
     */
    fun updateAvailableSpace(windowWidthDp: Float, windowHeightDp: Float) {
        val maxLeft = (windowWidthDp - MIN_CANVAS_DP - NAV_RAIL_WIDTH_DP - (if (!right.collapsed) right.minSizeDp else 0f)).coerceAtLeast(left.minSizeDp)
        val maxRight = (windowWidthDp - MIN_CANVAS_DP - NAV_RAIL_WIDTH_DP - (if (!left.collapsed) left.minSizeDp else 0f)).coerceAtLeast(right.minSizeDp)
        val maxBottom = (windowHeightDp - 160f).coerceAtLeast(bottom.minSizeDp)

        left.availableSpaceDp = maxLeft
        right.availableSpaceDp = maxRight
        bottom.availableSpaceDp = maxBottom
    }

    fun computeCanvasWidth(windowWidthDp: Float): Float {
        val panelsWidth = NAV_RAIL_WIDTH_DP + (if (!left.collapsed) left.sizeDp else 0f) + (if (!right.collapsed) right.sizeDp else 0f)
        return (windowWidthDp - panelsWidth).coerceAtLeast(0f)
    }

    fun computeCanvasHeight(windowHeightDp: Float): Float {
        val panelsHeight = if (!bottom.collapsed) bottom.sizeDp else 0f
        return (windowHeightDp - panelsHeight).coerceAtLeast(0f)
    }
}

@Composable
fun rememberShellPanelsController(): ShellPanelsController {
    val scope = rememberCoroutineScope()
    return remember { ShellPanelsController(coroutineScope = scope) }
}
