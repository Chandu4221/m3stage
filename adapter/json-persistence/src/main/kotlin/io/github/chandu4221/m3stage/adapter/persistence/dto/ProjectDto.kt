package io.github.chandu4221.m3stage.adapter.persistence.dto

import io.github.chandu4221.m3stage.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs are used strictly for serialization.
 * They prevent tying the domain model to @Serializable annotations.
 */

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val basePackage: String,
    val screens: List<ScreenDto>
)

@Serializable
data class ScreenDto(
    val id: String,
    val name: String,
    val route: String,
    val root: DesignNodeDto
)

@Serializable
data class DesignNodeDto(
    val id: String,
    val type: String,
    val props: Map<String, PropValDto> = emptyMap(),
    val modifiers: List<ModifierNodeDto> = emptyList(),
    val children: List<DesignNodeDto> = emptyList(),
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
            val topStart: Float, val topEnd: Float, val bottomEnd: Float, val bottomStart: Float
        ) : ShapeValDto
    }
}

@Serializable
sealed interface ModifierNodeDto {
    @Serializable
    @SerialName("mod_padding")
    data class Padding(val start: Float, val top: Float, val end: Float, val bottom: Float) : ModifierNodeDto

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
    @SerialName("mod_bg")
    data class Background(val color: PropValDto.ColorValDto) : ModifierNodeDto

    @Serializable
    @SerialName("mod_clip")
    data class Clip(val shape: PropValDto.ShapeValDto) : ModifierNodeDto
}

// --- Mappers: Convert between Domain and DTO ---

fun Project.toDto(): ProjectDto = ProjectDto(
    id = this.id.value,
    name = this.name,
    basePackage = this.basePackage,
    screens = this.screens.map { it.toDto() }
)

fun ProjectDto.toDomain(): Project = Project(
    id = ProjectId(this.id),
    name = this.name,
    basePackage = this.basePackage,
    screens = this.screens.map { it.toDomain() }
)

fun Screen.toDto(): ScreenDto = ScreenDto(
    id = this.id.value,
    name = this.name,
    route = this.route,
    root = this.root.toDto()
)

fun ScreenDto.toDomain(): Screen = Screen(
    id = ScreenId(this.id),
    name = this.name,
    route = this.route,
    root = this.root.toDomain()
)

fun DesignNode.toDto(): DesignNodeDto = DesignNodeDto(
    id = this.id.value,
    type = this.type.value,
    // Transform both key (PropId -> String) and value (PropVal -> PropValDto)
    props = this.props.map { (key, value) -> key.value to value.toDto() }.toMap(),
    modifiers = this.modifiers.map { it.toDto() },
    children = this.children.map { it.toDto() },
    isVisible = this.isVisible
)

// FLATTENED when expression to fix smart-cast issues with nested sealed interfaces
fun PropVal.toDto(): PropValDto = when (this) {
    is PropVal.Str -> PropValDto.Str(this.value)
    is PropVal.Bool -> PropValDto.Bool(this.value)
    is PropVal.Num -> PropValDto.Num(this.value)
    is PropVal.DpVal -> PropValDto.DpVal(this.value)
    is PropVal.SpVal -> PropValDto.SpVal(this.value)
    is PropVal.EnumVal -> PropValDto.EnumVal(this.name)
    is PropVal.ColorVal.Token -> PropValDto.ColorValDto.Token(this.token)
    is PropVal.ColorVal.Custom -> PropValDto.ColorValDto.Custom(this.argb)
    is PropVal.TypographyVal.Token -> PropValDto.TypographyValDto.Token(this.token)
    is PropVal.ShapeVal.Token -> PropValDto.ShapeValDto.Token(this.token)
    is PropVal.ShapeVal.UniformDp -> PropValDto.ShapeValDto.UniformDp(this.cornerRadius)
    is PropVal.ShapeVal.CornerDp -> PropValDto.ShapeValDto.CornerDp(
        this.topStart,
        this.topEnd,
        this.bottomEnd,
        this.bottomStart
    )
}

fun ModifierNode.toDto(): ModifierNodeDto = when (this) {
    is ModifierNode.Padding -> ModifierNodeDto.Padding(start, top, end, bottom)
    is ModifierNode.FillMaxWidth -> ModifierNodeDto.FillMaxWidth(fraction)
    is ModifierNode.FillMaxHeight -> ModifierNodeDto.FillMaxHeight(fraction)
    is ModifierNode.FillMaxSize -> ModifierNodeDto.FillMaxSize(fraction)
    is ModifierNode.Background -> ModifierNodeDto.Background(color.toDto() as PropValDto.ColorValDto)
    is ModifierNode.Clip -> ModifierNodeDto.Clip(shape.toDto() as PropValDto.ShapeValDto)
}

// --- Mappers: DTO to Domain ---

fun DesignNodeDto.toDomain(): DesignNode = DesignNode(
    id = NodeId(this.id),
    type = ComponentType(this.type),
    // Transform both key (String -> PropId) and value (PropValDto -> PropVal)
    props = this.props.map { (key, value) -> PropId(key) to value.toDomain() }.toMap(),
    modifiers = this.modifiers.map { it.toDomain() },
    children = this.children.map { it.toDomain() },
    isVisible = this.isVisible
)

// FLATTENED when expression for Domain reconstruction
fun PropValDto.toDomain(): PropVal = when (this) {
    is PropValDto.Str -> PropVal.Str(this.value)
    is PropValDto.Bool -> PropVal.Bool(this.value)
    is PropValDto.Num -> PropVal.Num(this.value)
    is PropValDto.DpVal -> PropVal.DpVal(this.value)
    is PropValDto.SpVal -> PropVal.SpVal(this.value)
    is PropValDto.EnumVal -> PropVal.EnumVal(this.name)
    is PropValDto.ColorValDto.Token -> PropVal.ColorVal.Token(this.token)
    is PropValDto.ColorValDto.Custom -> PropVal.ColorVal.Custom(this.argb)
    is PropValDto.TypographyValDto.Token -> PropVal.TypographyVal.Token(this.token)
    is PropValDto.ShapeValDto.Token -> PropVal.ShapeVal.Token(this.token)
    is PropValDto.ShapeValDto.UniformDp -> PropVal.ShapeVal.UniformDp(this.cornerRadius)
    is PropValDto.ShapeValDto.CornerDp -> PropVal.ShapeVal.CornerDp(
        this.topStart,
        this.topEnd,
        this.bottomEnd,
        this.bottomStart
    )
}

fun ModifierNodeDto.toDomain(): ModifierNode = when (this) {
    is ModifierNodeDto.Padding -> ModifierNode.Padding(start, top, end, bottom)
    is ModifierNodeDto.FillMaxWidth -> ModifierNode.FillMaxWidth(fraction)
    is ModifierNodeDto.FillMaxHeight -> ModifierNode.FillMaxHeight(fraction)
    is ModifierNodeDto.FillMaxSize -> ModifierNode.FillMaxSize(fraction)
    is ModifierNodeDto.Background -> ModifierNode.Background(color.toDomain() as PropVal.ColorVal)
    is ModifierNodeDto.Clip -> ModifierNode.Clip(shape.toDomain() as PropVal.ShapeVal)
}