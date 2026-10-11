package io.github.chandu4221.m3stage.modifier

/**
 * Visual modifier categories based on the official Android Compose specification:
 * https://developer.android.com/develop/ui/compose/modifiers-list
 *
 * Strictly includes visual layout, geometry, and styling categories.
 * Excludes logic-attaching categories (Actions, Focus, Pointer, Keyboard, Testing).
 */
enum class ModifierCategory(val displayName: String) {
    Padding(displayName = "Padding"),
    Size(displayName = "Size & Sizing"),
    Position(displayName = "Position & Order"),
    Drawing(displayName = "Drawing & Border"),
    Graphics(displayName = "Graphics Layer"),
    Transformations(displayName = "Transformations"),
    Scroll(displayName = "Scrolling")
}