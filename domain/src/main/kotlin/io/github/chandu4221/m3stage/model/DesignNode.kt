package io.github.chandu4221.m3stage.model

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertyValue

/**
 * Concrete AST node in the visual design graph.
 * Represents an individual component instance placed on a screen.
 */
data class DesignNode(
    val id: NodeId,
    val kind: ComponentKind,
    val props: Map<PropertyId, PropertyValue> = emptyMap(),
    val modifiers: List<ModifierSpec> = emptyList(),
    val children: List<DesignNode> = emptyList(),
    val slots: Map<SlotId, List<DesignNode>> = emptyMap(),
    val isVisible: Boolean = true
) {
    /**
     * Typed property lookup.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <V : PropertyValue> get(key: PropertyKey<V>): V? = props[key.id] as? V

    /**
     * Aggregates all direct child nodes across raw children and all named slots.
     */
    val allDirectChildren: List<DesignNode>
        get() = children + slots.values.flatten()

    /**
     * Recursively searches this subtree for a node with the specified [targetId].
     */
    fun findNode(targetId: NodeId): DesignNode? {
        if (id == targetId) return this
        for (child in allDirectChildren) {
            val found = child.findNode(targetId)
            if (found != null) return found
        }
        return null
    }

    /**
     * Checks if this node or any of its descendants matches [targetId].
     */
    fun contains(targetId: NodeId): Boolean = findNode(targetId) != null

    /**
     * Identifies which [SlotId] contains the child with [childId], if any.
     */
    fun findSlotForNode(childId: NodeId): SlotId? {
        for ((slotId, nodes) in slots) {
            if (nodes.any { it.id == childId }) return slotId
        }
        return null
    }
}