package io.github.chandu4221.m3stage.tooling.generator.model

import kotlinx.serialization.Serializable

@Serializable
enum class ComponentOriginKind {
    Component,
    DefaultsMember
}

@Serializable
data class ComponentOverload(
    val signatureId: String,
    val isDeprecated: Boolean = false,
    val optIn: List<String> = emptyList(), // e.g. ["ExperimentalMaterial3Api", "ExperimentalMaterial3ExpressiveApi"]
    val receiver: String? = null, // e.g. "RowScope" for extension composables
    val parameters: List<RawParameter> = emptyList(),
    val callbacks: List<RawCallback> = emptyList(),
    val slots: List<RawSlot> = emptyList(),
    val hasModifier: Boolean = true,
    val isContainer: Boolean = false
)

@Serializable
data class RawM3Component(
    val name: String,
    val packageName: String,
    val originKind: ComponentOriginKind = ComponentOriginKind.Component,
    val owner: String? = null, // e.g. "ButtonDefaults", "SliderDefaults"
    val overloads: List<ComponentOverload> = emptyList()
)