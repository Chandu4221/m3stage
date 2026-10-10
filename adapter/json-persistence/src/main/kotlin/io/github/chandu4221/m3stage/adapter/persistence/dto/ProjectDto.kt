package io.github.chandu4221.m3stage.adapter.persistence.dto

import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs are used strictly for serialization.
 * They prevent tying domain models to serialization annotations and library schemas.
 */

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val basePackage: String,
    val defaultDevice: String = "pixel_8",
    val seedColor: Long = 0xFF6750A4L,
    val isDarkMode: Boolean = false,
    val screens: List<ScreenDto>
)

@Serializable
data class ScreenDto(
    val id: String,
    val name: String,
    val route: String,
    val device: String? = null,
    val root: DesignNodeDto
)

@Serializable
data class DesignNodeDto(
    val id: String,
    val type: String,
    val props: Map<String, PropValDto> = emptyMap(),
    val modifiers: List<ModifierNodeDto> = emptyList(),
    val children: List<DesignNodeDto> = emptyList(),
    val slots: Map<String, List<DesignNodeDto>> = emptyMap(),
    val isVisible: Boolean = true
)

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

@Serializable
sealed interface ModifierNodeDto {
    @Serializable
    @SerialName("mod_padding")
    data class Padding(
        val start: Float,
        val top: Float,
        val end: Float,
        val bottom: Float
    ) : ModifierNodeDto

    @Serializable
    @SerialName("mod_fill_w")
    data class FillMaxWidth(val fraction: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_fill_h")
    data class FillMaxHeight(val fraction: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_fill_size")
    data class FillMaxSize(val fraction: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_size")
    data class Size(val width: Float, val height: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_width")
    data class Width(val width: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_height")
    data class Height(val height: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_wrap_content")
    data class WrapContentSize(val unbounded: Boolean = false) : ModifierNodeDto

    @Serializable
    @SerialName("mod_bg")
    data class Background(
        val color: PropValDto.ColorValDto,
        val shape: PropValDto.ShapeValDto? = null
    ) : ModifierNodeDto

    @Serializable
    @SerialName("mod_border")
    data class Border(
        val width: Float,
        val color: PropValDto.ColorValDto,
        val shape: PropValDto.ShapeValDto
    ) : ModifierNodeDto

    @Serializable
    @SerialName("mod_clip")
    data class Clip(val shape: PropValDto.ShapeValDto) : ModifierNodeDto

    @Serializable
    @SerialName("mod_shadow")
    data class Shadow(
        val elevation: Float,
        val shape: PropValDto.ShapeValDto,
        val clip: Boolean
    ) : ModifierNodeDto

    @Serializable
    @SerialName("mod_alpha")
    data class Alpha(val alpha: Float) : ModifierNodeDto

    @Serializable
    @SerialName("mod_offset")
    data class Offset(val x: Float, val y: Float) : ModifierNodeDto
}