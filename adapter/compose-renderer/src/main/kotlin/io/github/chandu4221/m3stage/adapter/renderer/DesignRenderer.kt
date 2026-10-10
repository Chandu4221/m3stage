package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.component.*
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

object DesignRenderer {

    private val registry: Map<ComponentKind, NodeRenderer> = mapOf(
        ComponentKind.Text to TextRenderer(),
        ComponentKind.Icon to IconRenderer(),
        ComponentKind.Image to ImageRenderer(),
        ComponentKind.Button to ButtonRenderer(),
        ComponentKind.TextField to TextFieldRenderer(),
        ComponentKind.Column to ColumnRenderer(),
        ComponentKind.Row to RowRenderer(),
        ComponentKind.Box to BoxRenderer(),
        ComponentKind.Card to CardRenderer(),
        ComponentKind.Scaffold to ScaffoldRenderer(),
        ComponentKind.TopAppBar to TopAppBarRenderer(),
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

            val isSelected = currentNode.id == selectedNodeId
            val isLocked = currentNode.id in lockedNodeIds

            SelectionOverlay(
                isSelected = isSelected,
                isLocked = isLocked,
                onClick = { onNodeClick(currentNode.id) }
            ) {
                val renderer = registry[currentNode.kind]
                if (renderer != null) {
                    val elementModifier = Modifier.resolveModifiers(currentNode.modifiers)
                    renderer.Render(currentNode, modifier = elementModifier) { child -> renderNode(child) }
                } else {
                    Text(text = "Unsupported: ${currentNode.kind.displayName}")
                }
            }
        }

        renderNode(node)
    }
}