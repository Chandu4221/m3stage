package io.github.chandu4221.m3stage.mutation

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

fun DesignNode.addNode(
    parentId: NodeId,
    child: DesignNode
): DesignNode {
}

fun DesignNode.removeNode(
    nodeId: NodeId
): DesignNode {
}

fun DesignNode.moveNode(
    nodeId: NodeId,
    newParentId: NodeId,
    index: Int
): DesignNode {
}

fun DesignNode.updateProp(
    nodeId: NodeId,
    key: String,
    value: String
): DesignNode {
}