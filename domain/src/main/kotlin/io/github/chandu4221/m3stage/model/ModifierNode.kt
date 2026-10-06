package io.github.chandu4221.m3stage.model

import io.github.chandu4221.m3stage.property.PropertyValue

sealed interface ModifierNode {
    data class Padding(
        val start: Float = 0f,
        val top: Float = 0f,
        val end: Float = 0f,
        val bottom: Float = 0f
    ) : ModifierNode

    data class FillMaxWidth(val fraction: Float = 1f) : ModifierNode
    data class FillMaxHeight(val fraction: Float = 1f) : ModifierNode
    data class FillMaxSize(val fraction: Float = 1f) : ModifierNode
    data class Background(val color: PropertyValue.ColorValue) : ModifierNode
    data class Clip(val shape: PropertyValue.ShapeValue) : ModifierNode
}