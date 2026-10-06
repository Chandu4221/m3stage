package io.github.chandu4221.m3stage.adapter.persistence.dto

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.ModifierNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyValue

// ==========================================
// Domain -> DTO Mappers
// ==========================================

fun Project.toDto(): ProjectDto = ProjectDto(
    id = this.id.value,
    name = this.name,
    basePackage = this.basePackage,
    screens = this.screens.map { it.toDto() }
)

fun Screen.toDto(): ScreenDto = ScreenDto(
    id = this.id.value,
    name = this.name,
    route = this.route,
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

fun ModifierNode.toDto(): ModifierNodeDto = when (this) {
    is ModifierNode.Padding -> ModifierNodeDto.Padding(start, top, end, bottom)
    is ModifierNode.FillMaxWidth -> ModifierNodeDto.FillMaxWidth(fraction)
    is ModifierNode.FillMaxHeight -> ModifierNodeDto.FillMaxHeight(fraction)
    is ModifierNode.FillMaxSize -> ModifierNodeDto.FillMaxSize(fraction)
    is ModifierNode.Background -> ModifierNodeDto.Background(color.toDto() as PropValDto.ColorValDto)
    is ModifierNode.Clip -> ModifierNodeDto.Clip(shape.toDto() as PropValDto.ShapeValDto)
}

// ==========================================
// DTO -> Domain Mappers
// ==========================================

fun ProjectDto.toDomain(): Project = Project(
    id = ProjectId(this.id),
    name = this.name,
    basePackage = this.basePackage,
    screens = this.screens.map { it.toDomain() }
)

fun ScreenDto.toDomain(): Screen = Screen(
    id = ScreenId(this.id),
    name = this.name,
    route = this.route,
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

fun ModifierNodeDto.toDomain(): ModifierNode = when (this) {
    is ModifierNodeDto.Padding -> ModifierNode.Padding(start, top, end, bottom)
    is ModifierNodeDto.FillMaxWidth -> ModifierNode.FillMaxWidth(fraction)
    is ModifierNodeDto.FillMaxHeight -> ModifierNode.FillMaxHeight(fraction)
    is ModifierNodeDto.FillMaxSize -> ModifierNode.FillMaxSize(fraction)
    is ModifierNodeDto.Background -> ModifierNode.Background(color.toDomain() as PropertyValue.ColorValue)
    is ModifierNodeDto.Clip -> ModifierNode.Clip(shape.toDomain() as PropertyValue.ShapeValue)
}