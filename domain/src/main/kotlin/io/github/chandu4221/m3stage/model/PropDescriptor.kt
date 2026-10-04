package io.github.chandu4221.m3stage.model

enum class PropGroup {
    Content, Layout, Appearance, Typography, Behavior
}

sealed interface PropDescriptor {
    val key: PropKey<*>
    val displayName: String
    val group: PropGroup

    data class Text(
        override val key: PropKey.StrKey,
        override val displayName: String,
        val defaultValue: PropVal.Str,
        override val group: PropGroup = PropGroup.Content
    ) : PropDescriptor

    data class Switch(
        override val key: PropKey.BoolKey,
        override val displayName: String,
        val defaultValue: PropVal.Bool,
        override val group: PropGroup = PropGroup.Behavior
    ) : PropDescriptor

    data class DpSlider(
        override val key: PropKey.DpKey,
        override val displayName: String,
        val defaultValue: PropVal.DpVal,
        val min: Float = 0f,
        val max: Float = 128f,
        override val group: PropGroup = PropGroup.Layout
    ) : PropDescriptor

    data class EnumDropdown(
        override val key: PropKey.EnumKey,
        override val displayName: String,
        val options: List<String>,
        val defaultValue: PropVal.EnumVal,
        override val group: PropGroup = PropGroup.Layout
    ) : PropDescriptor

    data class ColorPicker(
        override val key: PropKey.ColorKey,
        override val displayName: String,
        val defaultValue: PropVal.ColorVal,
        override val group: PropGroup = PropGroup.Appearance
    ) : PropDescriptor

    data class TypographyPicker(
        override val key: PropKey.TypographyKey,
        override val displayName: String,
        val defaultValue: PropVal.TypographyVal,
        override val group: PropGroup = PropGroup.Typography
    ) : PropDescriptor

    data class ShapePicker(
        override val key: PropKey.ShapeKey,
        override val displayName: String,
        val defaultValue: PropVal.ShapeVal,
        override val group: PropGroup = PropGroup.Appearance
    ) : PropDescriptor
}