package io.github.chandu4221.m3stage.model

import io.github.chandu4221.m3stage.property.PropertyValue

sealed interface ModifierNode {
    // --- SPACING & SIZING ---
    data class Padding(
        val start: Float = 0f,
        val top: Float = 0f,
        val end: Float = 0f,
        val bottom: Float = 0f
    ) : ModifierNode

    data class FillMaxWidth(val fraction: Float = 1f) : ModifierNode
    data class FillMaxHeight(val fraction: Float = 1f) : ModifierNode
    data class FillMaxSize(val fraction: Float = 1f) : ModifierNode

    data class Size(val width: Float, val height: Float) : ModifierNode
    data class Width(val width: Float) : ModifierNode
    data class Height(val height: Float) : ModifierNode
    data class WrapContentSize(val unbounded: Boolean = false) : ModifierNode

    // --- APPEARANCE & DRAWING ---
    data class Background(
        val color: PropertyValue.ColorValue,
        val shape: PropertyValue.ShapeValue? = null
    ) : ModifierNode

    data class Border(
        val width: Float = 1f,
        val color: PropertyValue.ColorValue,
        val shape: PropertyValue.ShapeValue = PropertyValue.ShapeValue.UniformDp(0f)
    ) : ModifierNode

    data class Clip(val shape: PropertyValue.ShapeValue) : ModifierNode

    data class Shadow(
        val elevation: Float = 4f,
        val shape: PropertyValue.ShapeValue = PropertyValue.ShapeValue.UniformDp(0f),
        val clip: Boolean = false
    ) : ModifierNode

    data class Alpha(val alpha: Float = 1f) : ModifierNode

    // --- INTERACTION & OFFSET ---
    data class Clickable(val enabled: Boolean = true) : ModifierNode
    data class Offset(val x: Float = 0f, val y: Float = 0f) : ModifierNode
}