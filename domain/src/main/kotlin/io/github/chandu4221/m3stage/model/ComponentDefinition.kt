package io.github.chandu4221.m3stage.model

data class ComponentDefinition(
    val type: ComponentType,
    val category: ComponentCategory,
    val displayName: String,
    val descriptors: List<PropDescriptor>,
    val defaultChildTypes: List<ComponentType> = emptyList(),
    val isContainer: Boolean = false
) {
    // Generates a strongly-typed default props map for new nodes
    fun createDefaultProps(): Map<PropId, PropVal> = descriptors.associate { descriptor ->
        val defaultValue = when (descriptor) {
            is PropDescriptor.Text -> descriptor.defaultValue
            is PropDescriptor.Switch -> descriptor.defaultValue
            is PropDescriptor.DpSlider -> descriptor.defaultValue
            is PropDescriptor.EnumDropdown -> descriptor.defaultValue
            is PropDescriptor.ColorPicker -> descriptor.defaultValue
            is PropDescriptor.TypographyPicker -> descriptor.defaultValue
            is PropDescriptor.ShapePicker -> descriptor.defaultValue
        }
        descriptor.key.id to defaultValue
    }
}