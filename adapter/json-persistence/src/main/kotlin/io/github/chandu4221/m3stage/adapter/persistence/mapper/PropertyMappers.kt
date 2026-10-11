package io.github.chandu4221.m3stage.adapter.persistence.mapper

import io.github.chandu4221.m3stage.adapter.persistence.dto.PropValDto
import io.github.chandu4221.m3stage.property.PropertyValue

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
