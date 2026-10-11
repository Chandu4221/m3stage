package io.github.chandu4221.m3stage.mutation

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.query.isDescendantOf

/**
 * Adds a child node to a parent node, either into a named [slotId] or into raw [children].
 * Validates domain containment rules via [canAcceptChild].
 * Returns a new immutable tree root.
 */
fun DesignNode.addNode(parentId: NodeId, child: DesignNode, slotId: SlotId? = null): DesignNode {
    if (this.id == parentId) {
        val def = ComponentCatalog.find(this.kind)
        if (def != null && !def.canAcceptChild(child.kind, slotId)) {
            val target = slotId?.value ?: "general children"
            throw IllegalArgumentException(
                "Parent '${this.kind.displayName}' cannot accept child of kind '${child.kind.displayName}' in $target."
            )
        }

        return if (slotId != null) {
            val existingSlotChildren = slots[slotId] ?: emptyList()
            copy(slots = slots + (slotId to (existingSlotChildren + child)))
        } else {
            copy(children = children + child)
        }
    }

    return mapDescendants { it.addNode(parentId, child, slotId) }
}

/**
 * Removes a node by ID from anywhere in the tree (whether in raw children or in a slot).
 * Returns a new immutable tree root.
 */
fun DesignNode.removeNode(nodeId: NodeId): DesignNode {
    val updatedChildren = children.filter { it.id != nodeId }.map { it.removeNode(nodeId) }
    val updatedSlots = slots.mapValues { (_, slotNodes) ->
        slotNodes.filter { it.id != nodeId }.map { it.removeNode(nodeId) }
    }
    return copy(children = updatedChildren, slots = updatedSlots)
}

/**
 * Updates a specific property on a node using a type-safe PropKey.
 * Returns a new immutable tree root.
 */
fun <V : PropertyValue> DesignNode.updateProp(nodeId: NodeId, key: PropertyKey<V>, value: V): DesignNode {
    if (this.id == nodeId) {
        return copy(props = props + (key.id to value))
    }
    return mapDescendants { it.updateProp(nodeId, key, value) }
}

/**
 * Updates a property by its raw PropId.
 * Used by Commands to avoid generic type erasure in the undo/redo stack.
 */
fun DesignNode.updatePropById(nodeId: NodeId, propId: PropertyId, value: PropertyValue): DesignNode {
    if (this.id == nodeId) {
        return copy(props = props + (propId to value))
    }
    return mapDescendants { it.updatePropById(nodeId, propId, value) }
}

/**
 * Removes a specific property key from a node.
 * Returns a new immutable tree root.
 */
fun DesignNode.removeProp(nodeId: NodeId, propId: PropertyId): DesignNode {
    if (this.id == nodeId) {
        return copy(props = props - propId)
    }
    return mapDescendants { it.removeProp(nodeId, propId) }
}

/**
 * Moves a node to a new parent at a specific index.
 * Enforces domain invariants (no cycles, valid IDs).
 */
fun DesignNode.moveNode(nodeId: NodeId, newParentId: NodeId, index: Int, targetSlotId: SlotId? = null): DesignNode {
    if (nodeId == newParentId) {
        throw IllegalArgumentException("Cannot move a node into itself.")
    }

    if (this.isDescendantOf(nodeId = newParentId, ancestorId = nodeId)) {
        throw IllegalArgumentException("Cannot drop a node into its own descendant.")
    }

    val nodeToMove = this.findNode(nodeId)
        ?: throw IllegalArgumentException("Node to move ($nodeId) not found.")

    val targetParent = this.findNode(newParentId)
        ?: throw IllegalArgumentException("Target parent ($newParentId) not found.")

    val def = ComponentCatalog.find(targetParent.kind)
    if (def != null && !def.canAcceptChild(nodeToMove.kind, targetSlotId)) {
        throw IllegalArgumentException(
            "Target '${targetParent.kind.displayName}' cannot accept '${nodeToMove.kind.displayName}'."
        )
    }

    val treeAfterRemoval = this.removeNode(nodeId)
    return treeAfterRemoval.insertNode(newParentId, nodeToMove, index, targetSlotId)
}

/**
 * Inserts a node at a specific index within a parent (in children or in a slot).
 */
private fun DesignNode.insertNode(
    parentId: NodeId,
    child: DesignNode,
    index: Int,
    slotId: SlotId? = null
): DesignNode {
    if (this.id == parentId) {
        return if (slotId != null) {
            val currentList = (slots[slotId] ?: emptyList()).toMutableList()
            val safeIndex = index.coerceIn(0, currentList.size)
            currentList.add(safeIndex, child)
            copy(slots = slots + (slotId to currentList.toList()))
        } else {
            val mutableChildren = children.toMutableList()
            val safeIndex = index.coerceIn(0, mutableChildren.size)
            mutableChildren.add(safeIndex, child)
            copy(children = mutableChildren.toList())
        }
    }
    return mapDescendants { it.insertNode(parentId, child, index, slotId) }
}

/**
 * Updates the visibility of a specific node anywhere in the tree.
 */
fun DesignNode.updateVisibility(nodeId: NodeId, isVisible: Boolean): DesignNode {
    if (this.id == nodeId) {
        return copy(isVisible = isVisible)
    }
    return mapDescendants { it.updateVisibility(nodeId, isVisible) }
}

/**
 * Appends a modifier to the node's modifier chain.
 */
fun DesignNode.addModifier(nodeId: NodeId, modifier: ModifierSpec): DesignNode {
    if (this.id == nodeId) {
        return copy(modifiers = modifiers + modifier)
    }
    return mapDescendants { it.addModifier(nodeId, modifier) }
}

/**
 * Removes a modifier at a specific index in the node's modifier chain.
 */
fun DesignNode.removeModifier(nodeId: NodeId, index: Int): DesignNode {
    if (this.id == nodeId) {
        if (index !in modifiers.indices) return this
        val mutable = modifiers.toMutableList()
        mutable.removeAt(index)
        return copy(modifiers = mutable.toList())
    }
    return mapDescendants { it.removeModifier(nodeId, index) }
}

/**
 * Updates a modifier at a specific index in the node's modifier chain.
 */
fun DesignNode.updateModifier(nodeId: NodeId, index: Int, modifier: ModifierSpec): DesignNode {
    if (this.id == nodeId) {
        if (index !in modifiers.indices) return this
        val mutable = modifiers.toMutableList()
        mutable[index] = modifier
        return copy(modifiers = mutable.toList())
    }
    return mapDescendants { it.updateModifier(nodeId, index, modifier) }
}

/**
 * Reorders a modifier in the chain from one position to another.
 */
fun DesignNode.reorderModifier(nodeId: NodeId, fromIndex: Int, toIndex: Int): DesignNode {
    if (this.id == nodeId) {
        if (fromIndex !in modifiers.indices || toIndex !in modifiers.indices || fromIndex == toIndex) {
            return this
        }
        val mutable = modifiers.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        return copy(modifiers = mutable.toList())
    }
    return mapDescendants { it.reorderModifier(nodeId, fromIndex, toIndex) }
}

/**
 * Sets the entire modifier list for a node.
 */
fun DesignNode.setModifiers(nodeId: NodeId, newModifiers: List<ModifierSpec>): DesignNode {
    if (this.id == nodeId) {
        return copy(modifiers = newModifiers)
    }
    return mapDescendants { it.setModifiers(nodeId, newModifiers) }
}

/**
 * Internal helper: Recursively maps both [children] and [slots] with identical transformation logic.
 */
private inline fun DesignNode.mapDescendants(transform: (DesignNode) -> DesignNode): DesignNode {
    val mappedChildren = children.map(transform)
    val mappedSlots = slots.mapValues { (_, slotChildren) -> slotChildren.map(transform) }
    return copy(children = mappedChildren, slots = mappedSlots)
}