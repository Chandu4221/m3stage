package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode


class RowRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        Row {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}