package io.github.chandu4221.m3stage.adapter.persistence.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Visual-only modifier serialization DTO hierarchy (20 M3 modifiers).
 */
@Serializable
sealed interface ModifierNodeDto {

    // =========================================================================
    // 1. PADDING
    // =========================================================================

    @Serializable
    @SerialName("mod_padding")
    data class Padding(
        val start: Float,
        val top: Float,
        val end: Float,
        val bottom: Float
    ) : ModifierNodeDto

    // =========================================================================
    // 2. SIZE
    // =========================================================================

    @Serializable
    @SerialName("mod_fill_w")
    data class FillMaxWidth(val fraction: Float = 1f) : ModifierNodeDto

    @Serializable
    @SerialName("mod_fill_h")
    data class FillMaxHeight(val fraction: Float = 1f) : ModifierNodeDto

    @Serializable
    @SerialName("mod_fill_size")
    data class FillMaxSize(val fraction: Float = 1f) : ModifierNodeDto

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
    @SerialName("mod_weight")
    data class Weight(val weight: Float = 1f, val fill: Boolean = true) : ModifierNodeDto

    @Serializable
    @SerialName("mod_aspect_ratio")
    data class AspectRatio(val ratio: Float = 1f) : ModifierNodeDto

    // =========================================================================
    // 3. POSITION & ORDER
    // =========================================================================

    @Serializable
    @SerialName("mod_offset")
    data class Offset(val x: Float = 0f, val y: Float = 0f) : ModifierNodeDto

    @Serializable
    @SerialName("mod_z_index")
    data class ZIndex(val zIndex: Float = 1f) : ModifierNodeDto

    // =========================================================================
    // 4. DRAWING & BORDER
    // =========================================================================

    @Serializable
    @SerialName("mod_bg")
    data class Background(
        val color: PropValDto.ColorValDto,
        val shape: PropValDto.ShapeValDto? = null
    ) : ModifierNodeDto

    @Serializable
    @SerialName("mod_border")
    data class Border(
        val width: Float = 1f,
        val color: PropValDto.ColorValDto,
        val shape: PropValDto.ShapeValDto
    ) : ModifierNodeDto

    @Serializable
    @SerialName("mod_shadow")
    data class Shadow(
        val elevation: Float = 4f,
        val shape: PropValDto.ShapeValDto,
        val clip: Boolean = false
    ) : ModifierNodeDto

    // =========================================================================
    // 5. GRAPHICS LAYER
    // =========================================================================

    @Serializable
    @SerialName("mod_clip")
    data class Clip(val shape: PropValDto.ShapeValDto) : ModifierNodeDto

    @Serializable
    @SerialName("mod_alpha")
    data class Alpha(val alpha: Float = 1f) : ModifierNodeDto

    // =========================================================================
    // 6. TRANSFORMATIONS
    // =========================================================================

    @Serializable
    @SerialName("mod_rotate")
    data class Rotate(val degrees: Float = 0f) : ModifierNodeDto

    @Serializable
    @SerialName("mod_scale")
    data class Scale(val scale: Float = 1f) : ModifierNodeDto

    // =========================================================================
    // 7. SCROLL
    // =========================================================================

    @Serializable
    @SerialName("mod_v_scroll")
    data class VerticalScroll(val enabled: Boolean = true) : ModifierNodeDto

    @Serializable
    @SerialName("mod_h_scroll")
    data class HorizontalScroll(val enabled: Boolean = true) : ModifierNodeDto
}
