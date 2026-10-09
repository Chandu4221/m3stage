package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.HorizontalArrangementOption
import io.github.chandu4221.m3stage.property.VerticalAlignmentOption

class RowRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val spacing = node[ComponentCatalog.RowProps.Spacing]?.value ?: 0f
        val hArrangement = when (node[ComponentCatalog.RowProps.HorizontalArrangement]?.name) {
            HorizontalArrangementOption.Center.name -> Arrangement.Center
            HorizontalArrangementOption.End.name -> Arrangement.End
            HorizontalArrangementOption.SpaceBetween.name -> Arrangement.SpaceBetween
            HorizontalArrangementOption.SpaceAround.name -> Arrangement.SpaceAround
            HorizontalArrangementOption.SpaceEvenly.name -> Arrangement.SpaceEvenly
            else -> Arrangement.spacedBy(spacing.dp)
        }
        val vAlign = when (node[ComponentCatalog.RowProps.VerticalAlignment]?.name) {
            VerticalAlignmentOption.Top.name -> Alignment.Top
            VerticalAlignmentOption.Bottom.name -> Alignment.Bottom
            else -> Alignment.CenterVertically
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = hArrangement,
            verticalAlignment = vAlign,
        ) {
            if (node.children.isEmpty()) {
                EmptyContainerPlaceholder("Row")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}