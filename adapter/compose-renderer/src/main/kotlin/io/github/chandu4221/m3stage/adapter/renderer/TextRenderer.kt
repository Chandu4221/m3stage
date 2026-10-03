package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode

/**
 * Renders a Text component.
 * Text is a leaf node - it has no children.
 */
class TextRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val text = node.props["text"] ?: "Text"
        Text(text = text)
    }
}