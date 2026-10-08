package io.github.chandu4221.m3stage.tooling.generator.model

import kotlinx.serialization.Serializable

@Serializable
enum class PropertyKind {
    Text,
    Switch,
    DpSlider,
    SpSlider,
    ColorPicker,
    ColorBundle,
    TypographyPicker,
    ShapePicker,
    EnumDropdown,
    Integer,
    FloatNumber,
    Modifier,
    Unknown
}

@Serializable
data class RawParameter(
    val name: String,
    val rawType: String,
    val isOptional: Boolean,
    val isNullable: Boolean,
    val kind: PropertyKind,
    val defaultsFactory: String? = null // e.g. "ButtonDefaults.buttonColors()" for ColorBundle
)

@Serializable
data class RawCallback(
    val name: String,
    val signature: String, // e.g. "() -> Unit", "(Boolean) -> Unit"
    val isOptional: Boolean
)