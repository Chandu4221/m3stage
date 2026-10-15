package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

data class NodeTreeInfo(
    val id: NodeId,
    val parent: NodeId?,
    val depth: Int,
    val isContainer: Boolean,
    val children: List<NodeId>
)

/**
 * Builds tree topology info for all nodes in the subtree of this [DesignNode].
 */
fun DesignNode.toTreeInfo(parent: NodeId? = null, depth: Int = 0): Map<NodeId, NodeTreeInfo> {
    val result = mutableMapOf<NodeId, NodeTreeInfo>()
    val directChildren = allDirectChildren
    result[this.id] = NodeTreeInfo(
        id = this.id,
        parent = parent,
        depth = depth,
        isContainer = this.kind.isContainer,
        children = directChildren.map { it.id }
    )
    for (child in directChildren) {
        result.putAll(child.toTreeInfo(parent = this.id, depth = depth + 1))
    }
    return result
}

/**
 * Registry of component on-screen bounds in artboard-local coordinate space.
 */
class NodeBoundsRegistry {
    private val bounds = mutableStateMapOf<NodeId, Rect>()

    fun report(id: NodeId, rect: Rect) {
        bounds[id] = rect
    }

    fun remove(id: NodeId) {
        bounds.remove(id)
    }

    fun clear() {
        bounds.clear()
    }

    operator fun get(id: NodeId): Rect? = bounds[id]

    fun all(): Map<NodeId, Rect> = bounds.toMap()

    /**
     * Returns the DEEPEST node containing [point] in artboard space.
     * Deepest = highest tree depth; tie-break by smaller area.
     */
    fun hitTest(
        point: Offset,
        treeInfo: Map<NodeId, NodeTreeInfo>
    ): NodeId? {
        if (!point.isSpecified) return null
        return bounds.entries
            .filter { (_, rect) -> rect.contains(point) }
            .mapNotNull { (id, rect) ->
                val info = treeInfo[id] ?: return@mapNotNull null
                val area = rect.width * rect.height
                Triple(id, info.depth, area)
            }
            .sortedWith(
                compareByDescending<Triple<NodeId, Int, Float>> { it.second } // depth descending
                    .thenBy { it.third } // smaller area wins
            )
            .firstOrNull()?.first
    }
}
