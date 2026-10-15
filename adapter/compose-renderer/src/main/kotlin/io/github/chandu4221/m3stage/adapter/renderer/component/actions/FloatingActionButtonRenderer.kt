package io.github.chandu4221.m3stage.adapter.renderer.component.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.FloatingActionButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.FabVariant
import io.github.chandu4221.m3stage.theme.M3ColorToken

class FloatingActionButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val variant = FloatingActionButtonDefinition.resolveVariant(node)
        val (defaultContainerToken, defaultContentToken) = when (variant) {
            FabVariant.Primary -> M3ColorToken.PrimaryContainer to M3ColorToken.OnPrimaryContainer
            FabVariant.Secondary -> M3ColorToken.SecondaryContainer to M3ColorToken.OnSecondaryContainer
            FabVariant.Tertiary -> M3ColorToken.TertiaryContainer to M3ColorToken.OnTertiaryContainer
        }

        val resolvedContainer = ThemeResolver.resolveColor(node[FloatingActionButtonDefinition.ContainerColor])
        val containerColor = if (resolvedContainer != Color.Unspecified) {
            resolvedContainer
        } else {
            ThemeResolver.resolveColorToken(defaultContainerToken)
        }

        val resolvedContent = ThemeResolver.resolveColor(node[FloatingActionButtonDefinition.ContentColor])
        val contentColor = if (resolvedContent != Color.Unspecified) {
            resolvedContent
        } else {
            ThemeResolver.resolveColorToken(defaultContentToken)
        }

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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
