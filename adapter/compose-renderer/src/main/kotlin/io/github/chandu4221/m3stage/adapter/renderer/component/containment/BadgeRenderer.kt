package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.BadgeDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class BadgeRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val containerColor = ThemeResolver.resolveColor(node[BadgeDefinition.ContainerColor])
        val contentColor = ThemeResolver.resolveColor(node[BadgeDefinition.ContentColor])
        val children = node.allDirectChildren

        Badge(
            modifier = modifier,
            containerColor = containerColor ?: androidx.compose.material3.MaterialTheme.colorScheme.error,
            contentColor = contentColor ?: androidx.compose.material3.MaterialTheme.colorScheme.onError
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
