package io.github.chandu4221.m3stage.property

sealed class PropertyKey<V : PropertyValue>(open val id: PropertyId) {
    data class StringKey(override val id: PropertyId) : PropertyKey<PropertyValue.StringValue>(id)
    data class BooleanKey(override val id: PropertyId) : PropertyKey<PropertyValue.BooleanValue>(id)
    data class NumberKey(override val id: PropertyId) : PropertyKey<PropertyValue.NumberValue>(id)
    data class DpKey(override val id: PropertyId) : PropertyKey<PropertyValue.DpValue>(id)
    data class SpKey(override val id: PropertyId) : PropertyKey<PropertyValue.SpValue>(id)
    data class EnumKey(override val id: PropertyId) : PropertyKey<PropertyValue.EnumValue>(id)
    data class ColorKey(override val id: PropertyId) : PropertyKey<PropertyValue.ColorValue>(id)
    data class TypographyKey(override val id: PropertyId) : PropertyKey<PropertyValue.TypographyValue>(id)
    data class ShapeKey(override val id: PropertyId) : PropertyKey<PropertyValue.ShapeValue>(id)
}