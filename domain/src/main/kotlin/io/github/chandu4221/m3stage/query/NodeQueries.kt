package io.github.chandu4221.m3stage.query

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

// Core lookup
fun DesignNode.findNode(id: NodeId): DesignNode? {
    // check if "id" is this node
    if (id == this.id) return this
    // check recursively
    return children.firstNotNullOfOrNull { it.findNode(id) }
}

fun DesignNode.findParent(id: NodeId): DesignNode? {
    // Traversing from the RootNode as there is no way to determine the parent.
    // checking each node its children has the id
    // "If the target ID is inside MY children list, then I AM the parent."
    if (children.any { it.id == id }) {
        return this
    }
    // "If it's not my direct child, let my children check if it's THEIR direct child."
    return children.firstNotNullOfOrNull { it.findParent(id) }
}

fun DesignNode.containsNode(id: NodeId): Boolean {
    return findNode(id) != null
}

// Path and ancestry
fun DesignNode.pathIdsTo(id: NodeId): List<NodeId>? {
    val pathIds: List<NodeId>? = null
}

//fun DesignNode.pathNodesTo(id: NodeId): List<DesignNode>? {}

//fun DesignNode.ancestorsOf(id: NodeId): List<DesignNode> {}

//fun DesignNode.depthOf(id: NodeId): Int? {}

//fun DesignNode.isRoot(id: NodeId): Boolean {}

// Position queries
fun DesignNode.findIndexInParent(id: NodeId): Int? {}

fun DesignNode.isDescendantOf(nodeId: NodeId, ancestorId: NodeId): Boolean {}

// Tree traversal
//fun DesignNode.flatten(): List<DesignNode> {}

//fun DesignNode.countNodes(): Int {}

// Type-based queries
//fun DesignNode.findFirstByType(type: ComponentType): DesignNode?{}

//fun DesignNode.findAllByType(type: ComponentType): List<DesignNode>{}

// Filtering
//fun DesignNode.filterNodes(predicate: (DesignNode) -> Boolean): List<DesignNode>{}

// Sibling queries
//fun DesignNode.nextSibling(id: NodeId): DesignNode?{}

//fun DesignNode.previousSibling(id: NodeId): DesignNode?{}

// Leaf/container checks
//fun DesignNode.isLeaf(id: NodeId): Boolean{}

//fun DesignNode.hasChildren(id: NodeId): Boolean{}

