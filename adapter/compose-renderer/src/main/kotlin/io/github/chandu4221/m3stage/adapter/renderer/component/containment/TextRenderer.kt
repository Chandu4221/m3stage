package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class TextRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val text = node[TextDefinition.TextContent]?.value ?: ""
        val color = ThemeResolver.resolveColor(node[TextDefinition.Color])
        val style = ThemeResolver.resolveTypography(node[TextDefinition.Typography])

        Text(
            text = text,
            color = color,
            style = style,
            modifier = modifier
        )
    }
}
