package io.github.chandu4221.m3stage.adapter.persistence.dto

import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Property values serialization hierarchy.
 */
@Serializable
sealed interface PropValDto {
    @Serializable
    @SerialName("str")
    data class Str(val value: String) : PropValDto

    @Serializable
    @SerialName("bool")
    data class Bool(val value: Boolean) : PropValDto

    @Serializable
    @SerialName("num")
    data class Num(val value: Double) : PropValDto

    @Serializable
    @SerialName("dp")
    data class DpVal(val value: Float) : PropValDto

    @Serializable
    @SerialName("sp")
    data class SpVal(val value: Float) : PropValDto

    @Serializable
    @SerialName("enum")
    data class EnumVal(val name: String) : PropValDto

    @Serializable
    sealed interface ColorValDto : PropValDto {
        @Serializable
        @SerialName("color_token")
        data class Token(val token: M3ColorToken) : ColorValDto

        @Serializable
        @SerialName("color_custom")
        data class Custom(val argb: Long) : ColorValDto
    }

    @Serializable
    sealed interface TypographyValDto : PropValDto {
        @Serializable
        @SerialName("typo_token")
        data class Token(val token: M3TypographyToken) : TypographyValDto
    }

    @Serializable
    sealed interface ShapeValDto : PropValDto {
        @Serializable
        @SerialName("shape_token")
        data class Token(val token: M3ShapeToken) : ShapeValDto

        @Serializable
        @SerialName("shape_uniform")
        data class UniformDp(val cornerRadius: Float) : ShapeValDto

        @Serializable
        @SerialName("shape_corner")
        data class CornerDp(
            val topStart: Float,
            val topEnd: Float,
            val bottomEnd: Float,
            val bottomStart: Float
        ) : ShapeValDto
    }
}
