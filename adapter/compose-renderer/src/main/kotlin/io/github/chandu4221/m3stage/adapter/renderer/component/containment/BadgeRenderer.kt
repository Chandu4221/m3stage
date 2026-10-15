package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.BadgeDefinition
import io.github.chandu4221.m3stage.model.DesignNode

import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.graphics.Color

class BadgeRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val resolvedContainer = ThemeResolver.resolveColor(node[BadgeDefinition.ContainerColor])
        val containerColor = if (resolvedContainer != Color.Unspecified) resolvedContainer else BadgeDefaults.containerColor
        val resolvedContent = ThemeResolver.resolveColor(node[BadgeDefinition.ContentColor])
        val contentColor = if (resolvedContent != Color.Unspecified) resolvedContent else contentColorFor(containerColor)
        val children = node.allDirectChildren

        Badge(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            if (children.isEmpty()) {
                Text(text = "1")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
