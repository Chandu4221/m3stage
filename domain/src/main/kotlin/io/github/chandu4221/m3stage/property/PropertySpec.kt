package io.github.chandu4221.m3stage.property

import io.github.chandu4221.m3stage.theme.M3ColorToken

sealed interface PropertySpec {
    val key: PropertyKey<*>
    val displayName: String
    val group: PropertyGroup

    data class Text(
        override val key: PropertyKey.StringKey,
        override val displayName: String,
        val defaultValue: PropertyValue.StringValue,
        override val group: PropertyGroup = PropertyGroup.Content
    ) : PropertySpec

    data class Switch(
        override val key: PropertyKey.BooleanKey,
        override val displayName: String,
        val defaultValue: PropertyValue.BooleanValue,
        override val group: PropertyGroup = PropertyGroup.Behavior
    ) : PropertySpec

    data class DpSlider(
        override val key: PropertyKey.DpKey,
        override val displayName: String,
        val defaultValue: PropertyValue.DpValue,
        val min: Float = 0f,
        val max: Float = 128f,
        override val group: PropertyGroup = PropertyGroup.Layout
    ) : PropertySpec

    data class EnumDropdown(
        override val key: PropertyKey.EnumKey,
        override val displayName: String,
        val options: List<String>,
        val defaultValue: PropertyValue.EnumValue,
        override val group: PropertyGroup = PropertyGroup.Layout
    ) : PropertySpec

    data class ColorPicker(
        override val key: PropertyKey.ColorKey,
        override val displayName: String,
        val defaultValue: PropertyValue.ColorValue = PropertyValue.ColorValue.Token(M3ColorToken.Inherit),
        override val group: PropertyGroup = PropertyGroup.Appearance
    ) : PropertySpec

    data class TypographyPicker(
        override val key: PropertyKey.TypographyKey,
        override val displayName: String,
        val defaultValue: PropertyValue.TypographyValue,
        override val group: PropertyGroup = PropertyGroup.Typography
    ) : PropertySpec

    data class ShapePicker(
        override val key: PropertyKey.ShapeKey,
        override val displayName: String,
        val defaultValue: PropertyValue.ShapeValue,
        override val group: PropertyGroup = PropertyGroup.Appearance
    ) : PropertySpec
}