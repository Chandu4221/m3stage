package io.github.chandu4221.m3stage.model

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.SlotId
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertyValue

data class DesignNode(
    val id: NodeId,
    val kind: ComponentKind,
    val props: Map<PropertyId, PropertyValue> = emptyMap(),
    val modifiers: List<ModifierSpec> = emptyList(),
    val children: List<DesignNode> = emptyList(),
    val slots: Map<SlotId, List<DesignNode>> = emptyMap(),
    val isVisible: Boolean = true
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <V : PropertyValue> get(key: PropertyKey<V>): V? = props[key.id] as? V
}