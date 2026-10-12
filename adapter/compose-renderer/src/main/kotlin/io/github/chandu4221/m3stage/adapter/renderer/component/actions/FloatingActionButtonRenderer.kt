package io.github.chandu4221.m3stage.adapter.renderer.component.actions

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.FloatingActionButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class FloatingActionButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val containerColor = ThemeResolver.resolveColor(node[FloatingActionButtonDefinition.ContainerColor])
            ?: FloatingActionButtonDefaults.containerColor
        val contentColor = ThemeResolver.resolveColor(node[FloatingActionButtonDefinition.ContentColor])
            ?: contentColorFor(containerColor)

        val iconNode = node.slots[FloatingActionButtonDefinition.IconSlot.id]?.firstOrNull()

        FloatingActionButton(
            onClick = {},
            containerColor = containerColor,
            contentColor = contentColor,
            modifier = modifier
        ) {
            if (iconNode != null) {
                renderChild(iconNode)
            } else if (node.children.isEmpty()) {
                Text(text = "+")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
