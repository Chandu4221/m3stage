package io.github.chandu4221.m3stage.adapter.persistence.dto

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.SlotId
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyValue

// ==========================================
// Domain -> DTO Mappers
// ==========================================

fun Project.toDto(): ProjectDto = ProjectDto(
    id = this.id.value,
    name = this.name,
    basePackage = this.basePackage,
    defaultDevice = this.defaultDevice.id,
    screens = this.screens.map { it.toDto() },
    seedColor = this.seedColor,
    isDarkMode = this.isDarkMode,
)

fun Screen.toDto(): ScreenDto = ScreenDto(
    id = this.id.value,
    name = this.name,
    route = this.route,
    device = this.device?.id,
    root = this.root.toDto()
)

fun DesignNode.toDto(): DesignNodeDto = DesignNodeDto(
    id = this.id.value,
    type = this.kind.id,
    props = this.props.map { (key, value) -> key.value to value.toDto() }.toMap(),
    modifiers = this.modifiers.map { it.toDto() },
    children = this.children.map { it.toDto() },
    slots = this.slots.map { (slotId, nodes) -> slotId.value to nodes.map { it.toDto() } }.toMap(),
    isVisible = this.isVisible
)

fun PropertyValue.toDto(): PropValDto = when (this) {
    is PropertyValue.StringValue -> PropValDto.Str(this.value)
    is PropertyValue.BooleanValue -> PropValDto.Bool(this.value)
    is PropertyValue.NumberValue -> PropValDto.Num(this.value)
    is PropertyValue.DpValue -> PropValDto.DpVal(this.value)
    is PropertyValue.SpValue -> PropValDto.SpVal(this.value)
    is PropertyValue.EnumValue -> PropValDto.EnumVal(this.name)
    is PropertyValue.ColorValue.Token -> PropValDto.ColorValDto.Token(this.token)
    is PropertyValue.ColorValue.Custom -> PropValDto.ColorValDto.Custom(this.argb)
    is PropertyValue.TypographyValue.Token -> PropValDto.TypographyValDto.Token(this.token)
    is PropertyValue.ShapeValue.Token -> PropValDto.ShapeValDto.Token(this.token)
    is PropertyValue.ShapeValue.UniformDp -> PropValDto.ShapeValDto.UniformDp(this.cornerRadius)
    is PropertyValue.ShapeValue.CornerDp -> PropValDto.ShapeValDto.CornerDp(
        this.topStart,
        this.topEnd,
        this.bottomEnd,
        this.bottomStart
    )
}

fun ModifierSpec.toDto(): ModifierNodeDto = when (this) {
    is ModifierSpec.Padding -> ModifierNodeDto.Padding(start, top, end, bottom)
    is ModifierSpec.FillMaxWidth -> ModifierNodeDto.FillMaxWidth(fraction)
    is ModifierSpec.FillMaxHeight -> ModifierNodeDto.FillMaxHeight(fraction)
    is ModifierSpec.FillMaxSize -> ModifierNodeDto.FillMaxSize(fraction)
    is ModifierSpec.Size -> ModifierNodeDto.Size(width, height)
    is ModifierSpec.Width -> ModifierNodeDto.Width(width)
    is ModifierSpec.Height -> ModifierNodeDto.Height(height)
    is ModifierSpec.WrapContentSize -> ModifierNodeDto.WrapContentSize(unbounded)
    is ModifierSpec.Background -> ModifierNodeDto.Background(
        color = color.toDto() as PropValDto.ColorValDto,
        shape = shape?.toDto() as? PropValDto.ShapeValDto
    )

    is ModifierSpec.Border -> ModifierNodeDto.Border(
        width = width,
        color = color.toDto() as PropValDto.ColorValDto,
        shape = shape.toDto() as PropValDto.ShapeValDto
    )

    is ModifierSpec.Clip -> ModifierNodeDto.Clip(shape.toDto() as PropValDto.ShapeValDto)
    is ModifierSpec.Shadow -> ModifierNodeDto.Shadow(
        elevation = elevation,
        shape = shape.toDto() as PropValDto.ShapeValDto,
        clip = clip
    )

    is ModifierSpec.Alpha -> ModifierNodeDto.Alpha(alpha)
    is ModifierSpec.Offset -> ModifierNodeDto.Offset(x, y)
}

// ==========================================
// DTO -> Domain Mappers
// ==========================================

fun ProjectDto.toDomain(): Project = Project(
    id = ProjectId(this.id),
    name = this.name,
    basePackage = this.basePackage,
    defaultDevice = DevicePreset.fromId(this.defaultDevice),
    screens = this.screens.map { it.toDomain() },
    seedColor = this.seedColor,
    isDarkMode = this.isDarkMode,
)

fun ScreenDto.toDomain(): Screen = Screen(
    id = ScreenId(this.id),
    name = this.name,
    route = this.route,
    device = this.device?.let { DevicePreset.fromId(it) },
    root = this.root.toDomain()
)

fun DesignNodeDto.toDomain(): DesignNode = DesignNode(
    id = NodeId(this.id),
    kind = ComponentKind.entries.firstOrNull { it.id == this.type } ?: ComponentKind.Box,
    props = this.props.map { (key, value) -> PropertyId(key) to value.toDomain() }.toMap(),
    modifiers = this.modifiers.map { it.toDomain() },
    children = this.children.map { it.toDomain() },
    slots = this.slots.map { (slotKey, nodes) -> SlotId(slotKey) to nodes.map { it.toDomain() } }.toMap(),
    isVisible = this.isVisible
)

fun PropValDto.toDomain(): PropertyValue = when (this) {
    is PropValDto.Str -> PropertyValue.StringValue(this.value)
    is PropValDto.Bool -> PropertyValue.BooleanValue(this.value)
    is PropValDto.Num -> PropertyValue.NumberValue(this.value)
    is PropValDto.DpVal -> PropertyValue.DpValue(this.value)
    is PropValDto.SpVal -> PropertyValue.SpValue(this.value)
    is PropValDto.EnumVal -> PropertyValue.EnumValue(this.name)
    is PropValDto.ColorValDto.Token -> PropertyValue.ColorValue.Token(this.token)
    is PropValDto.ColorValDto.Custom -> PropertyValue.ColorValue.Custom(this.argb)
    is PropValDto.TypographyValDto.Token -> PropertyValue.TypographyValue.Token(this.token)
    is PropValDto.ShapeValDto.Token -> PropertyValue.ShapeValue.Token(this.token)
    is PropValDto.ShapeValDto.UniformDp -> PropertyValue.ShapeValue.UniformDp(this.cornerRadius)
    is PropValDto.ShapeValDto.CornerDp -> PropertyValue.ShapeValue.CornerDp(
        this.topStart,
        this.topEnd,
        this.bottomEnd,
        this.bottomStart
    )
}

fun ModifierNodeDto.toDomain(): ModifierSpec = when (this) {
    is ModifierNodeDto.Padding -> ModifierSpec.Padding(start, top, end, bottom)
    is ModifierNodeDto.FillMaxWidth -> ModifierSpec.FillMaxWidth(fraction)
    is ModifierNodeDto.FillMaxHeight -> ModifierSpec.FillMaxHeight(fraction)
    is ModifierNodeDto.FillMaxSize -> ModifierSpec.FillMaxSize(fraction)
    is ModifierNodeDto.Size -> ModifierSpec.Size(width, height)
    is ModifierNodeDto.Width -> ModifierSpec.Width(width)
    is ModifierNodeDto.Height -> ModifierSpec.Height(height)
    is ModifierNodeDto.WrapContentSize -> ModifierSpec.WrapContentSize(unbounded)
    is ModifierNodeDto.Background -> ModifierSpec.Background(
        color = color.toDomain() as PropertyValue.ColorValue,
        shape = shape?.toDomain() as? PropertyValue.ShapeValue
    )

    is ModifierNodeDto.Border -> ModifierSpec.Border(
        width = width,
        color = color.toDomain() as PropertyValue.ColorValue,
        shape = shape.toDomain() as PropertyValue.ShapeValue
    )

    is ModifierNodeDto.Clip -> ModifierSpec.Clip(shape.toDomain() as PropertyValue.ShapeValue)
    is ModifierNodeDto.Shadow -> ModifierSpec.Shadow(
        elevation = elevation,
        shape = shape.toDomain() as PropertyValue.ShapeValue,
        clip = clip
    )

    is ModifierNodeDto.Alpha -> ModifierSpec.Alpha(alpha)
    is ModifierNodeDto.Offset -> ModifierSpec.Offset(x, y)
}