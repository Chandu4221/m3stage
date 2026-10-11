package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.model.DesignNode

class BoxRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val children = node.allDirectChildren
        Box(modifier = modifier) {
            if (children.isEmpty()) {
                EmptyContainerPlaceholder("Box")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
