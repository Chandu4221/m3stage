package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.ColumnDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class ColumnRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val spacing = node[ColumnDefinition.Spacing]?.value ?: 0f
        val children = node.allDirectChildren

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(spacing.dp)
        ) {
            if (children.isEmpty()) {
                EmptyContainerPlaceholder("Column")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
