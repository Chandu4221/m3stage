package io.github.chandu4221.m3stage.property

import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

sealed interface PropertyValue {
    data class StringValue(val value: String) : PropertyValue
    data class BooleanValue(val value: Boolean) : PropertyValue
    data class NumberValue(val value: Double) : PropertyValue
    data class DpValue(val value: Float) : PropertyValue
    data class SpValue(val value: Float) : PropertyValue
    data class EnumValue(val name: String) : PropertyValue
    sealed interface ColorValue : PropertyValue {
        data class Token(val token: M3ColorToken) : ColorValue
        data class Custom(val argb: Long) : ColorValue
    }

    sealed interface TypographyValue : PropertyValue {
        data class Token(val token: M3TypographyToken) : TypographyValue
    }

    sealed interface ShapeValue : PropertyValue {
        data class Token(val token: M3ShapeToken) : ShapeValue
        data class UniformDp(val cornerRadius: Float) : ShapeValue
        data class CornerDp(
            val topStart: Float = 0f,
            val topEnd: Float = 0f,
            val bottomEnd: Float = 0f,
            val bottomStart: Float = 0f
        ) : ShapeValue
    }
}