package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.registry.ComponentRendererRegistry
import io.github.chandu4221.m3stage.adapter.renderer.resolver.resolveModifiers
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

object DesignRenderer {

    @Composable
    fun Render(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId> = emptySet(),
        scheme: ColorScheme? = null
    ) {
        val content = @Composable {
            RenderInternal(node, selectedNodeId, onNodeClick, lockedNodeIds)
        }

        if (scheme != null) {
            MaterialTheme(colorScheme = scheme) {
                content()
            }
        } else {
            content()
        }
    }

    @Composable
    private fun RenderInternal(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId>
    ) {
        @Composable
        fun renderNode(currentNode: DesignNode, parentNode: DesignNode? = null) {
            if (!currentNode.isVisible) return

            val isSelected = currentNode.id == selectedNodeId
            val isLocked = currentNode.id in lockedNodeIds

            SelectionOverlay(
                isSelected = isSelected,
                isLocked = isLocked,
                onClick = { onNodeClick(currentNode.id) }
            ) {
                val renderer = ComponentRendererRegistry[currentNode.kind]
                if (renderer != null) {
                    val elementModifier = Modifier.resolveModifiers(currentNode.modifiers, parentNode?.kind)
                    renderer.Render(currentNode, modifier = elementModifier) { child -> renderNode(child, currentNode) }
                } else {
                    Text(text = "Unsupported: ${currentNode.kind.displayName}")
                }
            }
        }

        renderNode(node, null)
    }
}
