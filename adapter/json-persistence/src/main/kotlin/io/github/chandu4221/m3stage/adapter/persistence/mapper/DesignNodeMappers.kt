package io.github.chandu4221.m3stage.adapter.persistence.mapper

import io.github.chandu4221.m3stage.adapter.persistence.dto.DesignNodeDto
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.property.PropertyId

fun DesignNode.toDto(): DesignNodeDto = DesignNodeDto(
    id = this.id.value,
    type = this.kind.id,
    props = this.props.map { (key, value) -> key.value to value.toDto() }.toMap(),
    modifiers = this.modifiers.map { it.toDto() },
    children = this.children.map { it.toDto() },
    slots = this.slots.map { (slotId, nodes) -> slotId.value to nodes.map { it.toDto() } }.toMap(),
    isVisible = this.isVisible,
    isPrefilled = this.isPrefilled,
    label = this.label
)

fun DesignNodeDto.toDomain(): DesignNode {
    val kind = ComponentKind.entries.firstOrNull { it.id == this.type }
        ?: throw io.github.chandu4221.m3stage.adapter.persistence.UnknownComponentKindException(this.type)
    return DesignNode(
        id = NodeId(this.id),
        kind = kind,
        props = this.props.map { (key, value) -> PropertyId(key) to value.toDomain() }.toMap(),
        modifiers = this.modifiers.map { it.toDomain() },
        children = this.children.map { it.toDomain() },
        slots = this.slots.map { (slotKey, nodes) -> SlotId(slotKey) to nodes.map { it.toDomain() } }.toMap(),
        isVisible = this.isVisible,
        isPrefilled = this.isPrefilled,
        label = this.label
    )
}
