package io.github.chandu4221.m3stage.mutation


import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.isDescendantOf

/**
 * Adds a child node to the end of the specified parent's children list.
 * Returns a new immutable tree root.
 */
fun DesignNode.addNode(parentId: NodeId, child: DesignNode): DesignNode {
    if (this.id == parentId) {
        return copy(children = children + child)
    }
    return copy(children = children.map { it.addNode(parentId, child) })
}

/**
 * Removes a node by ID from anywhere in the tree.
 * Returns a new immutable tree root.
 */
fun DesignNode.removeNode(nodeId: NodeId): DesignNode {
    return copy(
        children = children
            .filter { it.id != nodeId }
            .map { it.removeNode(nodeId) }
    )
}

/**
 * Updates a specific property key-value pair on the target node.
 * Returns a new immutable tree root.
 */
fun DesignNode.updateProp(nodeId: NodeId, key: String, value: String): DesignNode {
    if (this.id == nodeId) {
        return copy(props = props + (key to value))
    }
    return copy(children = children.map { it.updateProp(nodeId, key, value) })
}

/**
 * Helper: Inserts a node at a specific index within a parent.
 */
private fun DesignNode.insertNode(parentId: NodeId, child: DesignNode, index: Int): DesignNode {
    if (this.id == parentId) {
        val mutableChildren = children.toMutableList()
        val safeIndex = index.coerceIn(0, mutableChildren.size)
        mutableChildren.add(safeIndex, child)
        return copy(children = mutableChildren)
    }
    return copy(children = children.map { it.insertNode(parentId, child, index) })
}

/**
 * Moves a node to a new parent at a specific index.
 * Enforces domain invariants (no cycles, valid IDs).
 * Throws IllegalArgumentException if the move is invalid.
 */
fun DesignNode.moveNode(nodeId: NodeId, newParentId: NodeId, index: Int): DesignNode {
    // 1. Prevent moving a node into itself
    if (nodeId == newParentId) {
        throw IllegalArgumentException("Cannot move a node into itself.")
    }

    // 2. Prevent circular references (Domain Invariant)
    // Checks if the new parent is a descendant of the node being moved.
    if (this.isDescendantOf(nodeId = newParentId, ancestorId = nodeId)) {
        throw IllegalArgumentException("Cannot drop a node into its own descendant.")
    }

    // 3. Validate existence
    val nodeToMove = this.findNode(nodeId)
        ?: throw IllegalArgumentException("Node to move ($nodeId) not found.")

    if (this.findNode(newParentId) == null) {
        throw IllegalArgumentException("Target parent ($newParentId) not found.")
    }

    // 4. Execute move: Remove then Insert
    val treeAfterRemoval = this.removeNode(nodeId)
    return treeAfterRemoval.insertNode(newParentId, nodeToMove, index)
}