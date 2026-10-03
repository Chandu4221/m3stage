package io.github.chandu4221.m3stage.model

data class ComponentDefinition(
    val type: ComponentType,
    val category: ComponentCategory,       // NEW: For palette grouping & codegen imports
    val displayName: String,
    val defaultProps: Map<String, String> = emptyMap(),
    val defaultChildTypes: List<ComponentType> = emptyList(),
    val isContainer: Boolean = false
)