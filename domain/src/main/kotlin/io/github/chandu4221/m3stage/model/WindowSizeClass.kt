package io.github.chandu4221.m3stage.model

/**
 * Material 3 Window Size Class breakpoints for adaptive layouts.
 * - Compact: width < 600dp (standard portrait phones)
 * - Medium: 600dp <= width < 840dp (tablets, foldables, landscape phones)
 * - Expanded: width >= 840dp (desktop, landscape tablets, unfolded foldables)
 */
enum class WindowSizeClass(val displayName: String) {
    Compact("Compact"),
    Medium("Medium"),
    Expanded("Expanded")
}

object WindowSizeClassResolver {
    /**
     * Resolves the Material 3 WindowSizeClass based on viewport width in Dp:
     * - Compact: < 600dp
     * - Medium: 600dp .. 839dp (< 840dp)
     * - Expanded: >= 840dp
     */
    fun fromWidthDp(widthDp: Int): WindowSizeClass = when {
        widthDp < 600 -> WindowSizeClass.Compact
        widthDp < 840 -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

/**
 * Reports the effective window size class for this screen at [widthDp].
 * All screens report their class for UI badges, while adaptive screens use it for layout decisions.
 */
fun Screen.effectiveWindowSizeClass(widthDp: Int): WindowSizeClass =
    WindowSizeClassResolver.fromWidthDp(widthDp)
