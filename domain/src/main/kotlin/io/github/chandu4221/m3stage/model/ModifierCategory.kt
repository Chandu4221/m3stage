package io.github.chandu4221.m3stage.model

/**
 * High-level visual modifier categories based on the official Android Compose specification:
 * https://developer.android.com/develop/ui/compose/modifiers-list
 *
 * Excludes logic-attaching categories (Actions, Focus, Pointer, Keyboard, Testing).
 */
enum class ModifierCategory(val displayName: String) {
    Padding(displayName = "Padding"),
    Size(displayName = "Size"),
    Position(displayName = "Position"),
    Drawing(displayName = "Drawing & Appearance"),
    Transformation(displayName = "Transformations")
}