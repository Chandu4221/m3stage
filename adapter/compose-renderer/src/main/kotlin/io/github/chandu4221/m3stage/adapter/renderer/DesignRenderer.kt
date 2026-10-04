package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

object DesignRenderer {

    private val registry: Map<String, NodeRenderer> = mapOf(
        "Text" to TextRenderer(),
        "Button" to ButtonRenderer(),
        "Column" to ColumnRenderer(),
        "Row" to RowRenderer(),
        "Box" to BoxRenderer(),
        "Card" to CardRenderer()
    )

    @Composable
    fun Render(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId> = emptySet() // <-- Added
    ) {
        @Composable
        fun renderNode(currentNode: DesignNode) {
            if (!currentNode.isVisible) return

            val isSelected = currentNode.id.value == selectedNodeId?.value
            val isLocked = currentNode.id in lockedNodeIds // <-- Check set

            SelectionOverlay(
                isSelected = isSelected,
                isLocked = isLocked,
                onClick = { onNodeClick(currentNode.id) }
            ) {
                val renderer = registry[currentNode.type.value]
                if (renderer != null) {
                    renderer.Render(currentNode) { child -> renderNode(child) }
                } else {
                    Text(text = "Unknown: ${currentNode.type.value}")
                }
            }
        }

        renderNode(node)
    }
}