package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.PropVal

class TextRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        // Type-safe extraction
        val textContent = (node[ComponentCatalog.TextProps.TextContent] as? PropVal.Str)?.value ?: "Sample Text"

        Text(
            text = textContent,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}