package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode


class BoxRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        Box {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}