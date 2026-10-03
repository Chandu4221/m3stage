package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode


/**
 * Renders a Column layout component.
 */
class ColumnRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        Column {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}