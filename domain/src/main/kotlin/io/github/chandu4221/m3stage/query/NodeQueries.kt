package io.github.chandu4221.m3stage.query

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

/**
 * Searches this node's subtree for a node with the specified [id].
 * Traverses both raw children and named slots.
 */
fun DesignNode.findNode(id: NodeId): DesignNode? {
    if (this.id == id) return this
    for (child in allDirectChildren) {
        val found = child.findNode(id)
        if (found != null) return found
    }
    return null
}

/**
 * Finds the parent node of [id] anywhere within this tree.
 * Traverses both raw children and named slots.
 */
fun DesignNode.findParent(id: NodeId): DesignNode? {
    if (allDirectChildren.any { it.id == id }) {
        return this
    }
    for (child in allDirectChildren) {
        val parent = child.findParent(id)
        if (parent != null) return parent
    }
    return null
}

/**
 * Checks if a node with [id] exists in this subtree.
 */
fun DesignNode.containsNode(id: NodeId): Boolean = findNode(id) != null

/**
 * Computes the path of [NodeId]s from this root down to the target [id].
 */
fun DesignNode.pathIdsTo(id: NodeId): List<NodeId>? {
    if (this.id == id) {
        return listOf(this.id)
    }

    for (child in allDirectChildren) {
        val childPath = child.pathIdsTo(id)
        if (childPath != null) {
            return listOf(this.id) + childPath
        }
    }

    return null
}

/**
 * Finds the index of a child node within its immediate parent.
 * Checks raw children first, then slots.
 */
fun DesignNode.findIndexInParent(id: NodeId): Int? {
    if (this.id == id) return null

    val indexInChildren = children.indexOfFirst { it.id == id }
    if (indexInChildren != -1) return indexInChildren

    for ((_, slotChildren) in slots) {
        val indexInSlot = slotChildren.indexOfFirst { it.id == id }
        if (indexInSlot != -1) return indexInSlot
    }

    for (child in allDirectChildren) {
        val result = child.findIndexInParent(id)
        if (result != null) return result
    }
    return null
}

/**
 * Checks if [nodeId] is a descendant of [ancestorId].
 */
fun DesignNode.isDescendantOf(nodeId: NodeId, ancestorId: NodeId): Boolean {
    if (nodeId == ancestorId) return false
    val ancestor = findNode(ancestorId) ?: return false
    return ancestor.containsNode(nodeId)
}

/**
 * Flattens the entire tree into a list of all nodes.
 */
fun DesignNode.flatten(): List<DesignNode> {
    val result = mutableListOf<DesignNode>()
    fun collect(node: DesignNode) {
        result.add(node)
        for (child in node.allDirectChildren) {
            collect(child)
        }
    }
    collect(this)
    return result
}