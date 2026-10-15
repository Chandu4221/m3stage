package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue

/**
 * Specification for typed component variants.
 */
data class VariantSpec<E : Enum<E>>(
    val key: PropertyKey.EnumKey,
    val defaultVariant: E,
    val entries: List<E>,
    val displayName: String = "Variant",
    val visiblePropsFor: (variant: E) -> Set<PropertyId>? = { null }
) {
    fun resolveVariant(node: DesignNode): E {
        val enumName = node[key]?.name
        return if (enumName != null) {
            entries.firstOrNull { it.name.equals(enumName, ignoreCase = true) } ?: defaultVariant
        } else {
            defaultVariant
        }
    }

    fun isPropVisible(node: DesignNode, propId: PropertyId): Boolean {
        val variant = resolveVariant(node)
        val allowed = visiblePropsFor(variant)
        return allowed == null || propId in allowed
    }
}

/**
 * Metamodel definition specifying a component's properties, slots, and containment rules.
 */
data class ComponentDefinition(
    val kind: ComponentKind,
    val displayName: String = kind.displayName,
    val category: ComponentCategory = kind.category,
    val properties: List<PropertySpec>,
    val slots: List<SlotDefinition> = emptyList(),
    val allowedChildren: Set<ComponentKind>? = null,
    val isContainer: Boolean = kind.isContainer,
    val slotOnly: Boolean = false,
    val showInPalette: Boolean = true,
    val variantSpec: VariantSpec<*>? = null,
    val defaultSubtreeFactory: ((self: ComponentDefinition, idGen: IdGenerator) -> DesignNode)? = null
) {
    /**
     * Determines whether [propId] is visible on [node] under its current variant.
     */
    fun isPropVisible(node: DesignNode, propId: PropertyId): Boolean =
        variantSpec?.isPropVisible(node, propId) ?: true

    /**
     * Generates a strongly-typed default properties map for newly dropped nodes.
     */
    fun createDefaultProps(): Map<PropertyId, PropertyValue> = properties.associate { spec ->
        val defaultValue = when (spec) {
            is PropertySpec.Text -> spec.defaultValue
            is PropertySpec.Switch -> spec.defaultValue
            is PropertySpec.DpSlider -> spec.defaultValue
            is PropertySpec.EnumDropdown -> spec.defaultValue
            is PropertySpec.ColorPicker -> spec.defaultValue
            is PropertySpec.TypographyPicker -> spec.defaultValue
            is PropertySpec.ShapePicker -> spec.defaultValue
        }
        spec.key.id to defaultValue
    }

    /**
     * Instantiates a concrete [DesignNode] with this component's canonical default anatomy.
     */
    fun createDefaultSubtree(idGen: IdGenerator): DesignNode {
        val root = defaultSubtreeFactory?.invoke(this, idGen) ?: DesignNode(
            id = idGen.nextNodeId(),
            kind = kind,
            props = createDefaultProps()
        )
        return root.markSubtreePrefilled()
    }

    private fun DesignNode.markSubtreePrefilled(): DesignNode {
        return this.copy(
            isPrefilled = true,
            children = children.map { it.markSubtreePrefilled() },
            slots = slots.mapValues { (_, nodes) -> nodes.map { it.markSubtreePrefilled() } }
        )
    }

    /**
     * Looks up a slot definition by its identifier.
     */
    fun findSlot(slotId: SlotId): SlotDefinition? =
        slots.firstOrNull { it.id == slotId }

    /**
     * Validates whether a child component of [childKind] can be placed inside this component,
     * either in a specific [slotId] or as a general child.
     */
    fun canAcceptChild(childKind: ComponentKind, slotId: SlotId? = null): Boolean {
        if (!isContainer) return false

        if (slotId != null) {
            val slot = findSlot(slotId) ?: return false
            return slot.canAccept(childKind)
        }

        return allowedChildren == null || childKind in allowedChildren
    }
}