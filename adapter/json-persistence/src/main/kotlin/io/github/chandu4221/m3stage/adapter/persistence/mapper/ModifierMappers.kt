package io.github.chandu4221.m3stage.adapter.persistence.mapper

import io.github.chandu4221.m3stage.adapter.persistence.dto.ModifierNodeDto
import io.github.chandu4221.m3stage.adapter.persistence.dto.PropValDto
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.property.PropertyValue

fun ModifierSpec.toDto(): ModifierNodeDto = when (this) {
    // 1. Padding
    is ModifierSpec.Padding -> ModifierNodeDto.Padding(start, top, end, bottom)

    // 2. Size
    is ModifierSpec.FillMaxWidth -> ModifierNodeDto.FillMaxWidth(fraction)
    is ModifierSpec.FillMaxHeight -> ModifierNodeDto.FillMaxHeight(fraction)
    is ModifierSpec.FillMaxSize -> ModifierNodeDto.FillMaxSize(fraction)
    is ModifierSpec.Size -> ModifierNodeDto.Size(width, height)
    is ModifierSpec.Width -> ModifierNodeDto.Width(width)
    is ModifierSpec.Height -> ModifierNodeDto.Height(height)
    is ModifierSpec.WrapContentSize -> ModifierNodeDto.WrapContentSize(unbounded)
    is ModifierSpec.Weight -> ModifierNodeDto.Weight(weight, fill)
    is ModifierSpec.AspectRatio -> ModifierNodeDto.AspectRatio(ratio)

    // 3. Position & Order
    is ModifierSpec.Offset -> ModifierNodeDto.Offset(x, y)
    is ModifierSpec.ZIndex -> ModifierNodeDto.ZIndex(zIndex)

    // 4. Drawing & Border
    is ModifierSpec.Background -> ModifierNodeDto.Background(
        color = color.toDto() as PropValDto.ColorValDto,
        shape = shape?.toDto() as? PropValDto.ShapeValDto
    )
    is ModifierSpec.Border -> ModifierNodeDto.Border(
        width = width,
        color = color.toDto() as PropValDto.ColorValDto,
        shape = shape.toDto() as PropValDto.ShapeValDto
    )
    is ModifierSpec.Shadow -> ModifierNodeDto.Shadow(
        elevation = elevation,
        shape = shape.toDto() as PropValDto.ShapeValDto,
        clip = clip
    )

    // 5. Graphics Layer
    is ModifierSpec.Clip -> ModifierNodeDto.Clip(shape.toDto() as PropValDto.ShapeValDto)
    is ModifierSpec.Alpha -> ModifierNodeDto.Alpha(alpha)

    // 6. Transformations
    is ModifierSpec.Rotate -> ModifierNodeDto.Rotate(degrees)
    is ModifierSpec.Scale -> ModifierNodeDto.Scale(scale)

    // 7. Scroll
    is ModifierSpec.VerticalScroll -> ModifierNodeDto.VerticalScroll(enabled)
    is ModifierSpec.HorizontalScroll -> ModifierNodeDto.HorizontalScroll(enabled)
}

fun ModifierNodeDto.toDomain(): ModifierSpec = when (this) {
    // 1. Padding
    is ModifierNodeDto.Padding -> ModifierSpec.Padding(start, top, end, bottom)

    // 2. Size
    is ModifierNodeDto.FillMaxWidth -> ModifierSpec.FillMaxWidth(fraction)
    is ModifierNodeDto.FillMaxHeight -> ModifierSpec.FillMaxHeight(fraction)
    is ModifierNodeDto.FillMaxSize -> ModifierSpec.FillMaxSize(fraction)
    is ModifierNodeDto.Size -> ModifierSpec.Size(width, height)
    is ModifierNodeDto.Width -> ModifierSpec.Width(width)
    is ModifierNodeDto.Height -> ModifierSpec.Height(height)
    is ModifierNodeDto.WrapContentSize -> ModifierSpec.WrapContentSize(unbounded)
    is ModifierNodeDto.Weight -> ModifierSpec.Weight(weight, fill)
    is ModifierNodeDto.AspectRatio -> ModifierSpec.AspectRatio(ratio)

    // 3. Position & Order
    is ModifierNodeDto.Offset -> ModifierSpec.Offset(x, y)
    is ModifierNodeDto.ZIndex -> ModifierSpec.ZIndex(zIndex)

    // 4. Drawing & Border
    is ModifierNodeDto.Background -> ModifierSpec.Background(
        color = color.toDomain() as PropertyValue.ColorValue,
        shape = shape?.toDomain() as? PropertyValue.ShapeValue
    )
    is ModifierNodeDto.Border -> ModifierSpec.Border(
        width = width,
        color = color.toDomain() as PropertyValue.ColorValue,
        shape = shape.toDomain() as PropertyValue.ShapeValue
    )
    is ModifierNodeDto.Shadow -> ModifierSpec.Shadow(
        elevation = elevation,
        shape = shape.toDomain() as PropertyValue.ShapeValue,
        clip = clip
    )

    // 5. Graphics Layer
    is ModifierNodeDto.Clip -> ModifierSpec.Clip(shape.toDomain() as PropertyValue.ShapeValue)
    is ModifierNodeDto.Alpha -> ModifierSpec.Alpha(alpha)

    // 6. Transformations
    is ModifierNodeDto.Rotate -> ModifierSpec.Rotate(degrees)
    is ModifierNodeDto.Scale -> ModifierSpec.Scale(scale)

    // 7. Scroll
    is ModifierNodeDto.VerticalScroll -> ModifierSpec.VerticalScroll(enabled)
    is ModifierNodeDto.HorizontalScroll -> ModifierSpec.HorizontalScroll(enabled)
}
