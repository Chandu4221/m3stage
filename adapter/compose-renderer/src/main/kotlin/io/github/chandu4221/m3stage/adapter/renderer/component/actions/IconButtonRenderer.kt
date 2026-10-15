package io.github.chandu4221.m3stage.adapter.renderer.component.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.IconButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class IconButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val enabled = node[IconButtonDefinition.Enabled]?.value ?: true
        val contentColor = ThemeResolver.resolveColor(node[IconButtonDefinition.ContentColor])

        val colors = if (contentColor != androidx.compose.ui.graphics.Color.Unspecified) {
            IconButtonDefaults.iconButtonColors(contentColor = contentColor)
        } else {
            IconButtonDefaults.iconButtonColors()
        }

        val iconNode = node.slots[IconButtonDefinition.IconSlot.id]?.firstOrNull()

        IconButton(
            onClick = {},
            enabled = enabled,
            colors = colors,
            modifier = modifier
        ) {
            if (iconNode != null) {
                renderChild(iconNode)
            } else if (node.children.isEmpty()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
