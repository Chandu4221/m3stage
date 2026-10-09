package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.TextOverflowOption

class TextRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val textContent = node[ComponentCatalog.TextProps.TextContent]?.value ?: "Sample Text"
        val typography = ThemeResolver.resolveTypography(node[ComponentCatalog.TextProps.Typography])
        val color = ThemeResolver.resolveColor(node[ComponentCatalog.TextProps.Color])
        val overflowOpt = node[ComponentCatalog.TextProps.Overflow]?.name

        val textOverflow = when (overflowOpt) {
            TextOverflowOption.Ellipsis.name -> TextOverflow.Ellipsis
            else -> TextOverflow.Clip
        }

        Text(
            text = textContent,
            style = typography,
            color = color,
            overflow = textOverflow,
            modifier = modifier
        )
    }
}