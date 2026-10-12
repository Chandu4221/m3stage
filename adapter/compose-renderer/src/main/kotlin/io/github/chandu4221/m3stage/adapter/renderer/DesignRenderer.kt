package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import io.github.chandu4221.m3stage.adapter.renderer.registry.ComponentRendererRegistry
import io.github.chandu4221.m3stage.adapter.renderer.resolver.resolveModifiers
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.adapter.renderer.selection.nodeBoundsReporter
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

private object NoIndication : androidx.compose.foundation.IndicationNodeFactory {
    override fun create(interactionSource: androidx.compose.foundation.interaction.InteractionSource): androidx.compose.ui.node.DelegatableNode {
        return object : androidx.compose.ui.Modifier.Node() {}
    }
    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

object DesignRenderer {

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    fun Render(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId> = emptySet(),
        scheme: ColorScheme? = null,
        registry: NodeBoundsRegistry? = null,
        artboardOrigin: () -> Offset = { Offset.Zero }
    ) {
        val content = @Composable {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.foundation.LocalIndication provides NoIndication,
                androidx.compose.material3.LocalRippleConfiguration provides null
            ) {
                RenderInternal(node, registry, artboardOrigin)
            }
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
        registry: NodeBoundsRegistry?,
        artboardOrigin: () -> Offset
    ) {
        @Composable
        fun renderNode(currentNode: DesignNode, parentNode: DesignNode? = null, depth: Int = 0) {
            if (!currentNode.isVisible) return

            val baseModifier = if (registry != null) {
                Modifier.nodeBoundsReporter(
                    nodeId = currentNode.id,
                    registry = registry,
                    artboardOrigin = artboardOrigin
                )
            } else {
                Modifier
            }

            Box(modifier = baseModifier) {
                val renderer = ComponentRendererRegistry[currentNode.kind]
                if (renderer != null) {
                    val elementModifier = Modifier.resolveModifiers(currentNode.modifiers, parentNode?.kind)
                    renderer.Render(currentNode, modifier = elementModifier) { child ->
                        renderNode(child, currentNode, depth + 1)
                    }
                } else {
                    Text(text = "Unsupported: ${currentNode.kind.displayName}")
                }
            }
        }

        renderNode(node, null, 0)
    }
}
