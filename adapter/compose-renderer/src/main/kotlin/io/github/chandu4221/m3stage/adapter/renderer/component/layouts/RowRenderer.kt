package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.RowDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class RowRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val spacing = node[RowDefinition.Spacing]?.value ?: 0f
        val children = node.allDirectChildren

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(spacing.dp)
        ) {
            if (children.isEmpty()) {
                EmptyContainerPlaceholder("Row")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
