package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.adapter.renderer.component.BoxRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.ButtonRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.CardRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.ColumnRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.RowRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.TextRenderer
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

object DesignRenderer {

    private val registry: Map<ComponentKind, NodeRenderer> = mapOf(
        ComponentKind.Text to TextRenderer(),
        ComponentKind.Button to ButtonRenderer(),
        ComponentKind.Column to ColumnRenderer(),
        ComponentKind.Row to RowRenderer(),
        ComponentKind.Box to BoxRenderer(),
        ComponentKind.Card to CardRenderer()
    )

    @Composable
    fun Render(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId> = emptySet()
    ) {
        @Composable
        fun renderNode(currentNode: DesignNode) {
            if (!currentNode.isVisible) return

            val isSelected = currentNode.id.value == selectedNodeId?.value
            val isLocked = currentNode.id in lockedNodeIds

            SelectionOverlay(
                isSelected = isSelected,
                isLocked = isLocked,
                onClick = { onNodeClick(currentNode.id) }
            ) {
                val renderer = registry[currentNode.kind]
                if (renderer != null) {
                    renderer.Render(currentNode) { child -> renderNode(child) }
                } else {
                    Text(text = "Unsupported: ${currentNode.kind.displayName}")
                }
            }
        }

        renderNode(node)
    }
}