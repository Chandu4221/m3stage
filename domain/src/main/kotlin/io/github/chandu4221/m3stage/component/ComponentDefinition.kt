package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.property.PropertyDescriptor
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyValue

data class ComponentDefinition(
    val kind: ComponentKind,
    val displayName: String = kind.displayName,
    val category: ComponentCategory = kind.category,
    val descriptors: List<PropertyDescriptor>,
    val slots: List<SlotDefinition> = emptyList(),
    val allowedChildren: Set<ComponentKind>? = null, // Enforces Atomic rules (e.g. Button only accepts Text & Icon)
    val isContainer: Boolean = kind.isContainer
) {
    /**
     * Generates a strongly-typed default properties map for newly dropped nodes.
     */
    fun createDefaultProps(): Map<PropertyId, PropertyValue> = descriptors.associate { descriptor ->
        val defaultValue = when (descriptor) {
            is PropertyDescriptor.Text -> descriptor.defaultValue
            is PropertyDescriptor.Switch -> descriptor.defaultValue
            is PropertyDescriptor.DpSlider -> descriptor.defaultValue
            is PropertyDescriptor.EnumDropdown -> descriptor.defaultValue
            is PropertyDescriptor.ColorPicker -> descriptor.defaultValue
            is PropertyDescriptor.TypographyPicker -> descriptor.defaultValue
            is PropertyDescriptor.ShapePicker -> descriptor.defaultValue
        }
        descriptor.key.id to defaultValue
    }
}