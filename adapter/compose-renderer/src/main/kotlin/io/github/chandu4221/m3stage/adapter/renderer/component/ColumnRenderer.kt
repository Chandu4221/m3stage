package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.HorizontalAlignmentOption
import io.github.chandu4221.m3stage.property.VerticalArrangementOption

class ColumnRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val spacing = node[ComponentCatalog.ColumnProps.Spacing]?.value ?: 0f
        val hAlign = when (node[ComponentCatalog.ColumnProps.HorizontalAlignment]?.name) {
            HorizontalAlignmentOption.CenterHorizontally.name -> Alignment.CenterHorizontally
            HorizontalAlignmentOption.End.name -> Alignment.End
            else -> Alignment.Start
        }
        val vArrangement = when (node[ComponentCatalog.ColumnProps.VerticalArrangement]?.name) {
            VerticalArrangementOption.Center.name -> Arrangement.Center
            VerticalArrangementOption.Bottom.name -> Arrangement.Bottom
            VerticalArrangementOption.SpaceBetween.name -> Arrangement.SpaceBetween
            VerticalArrangementOption.SpaceAround.name -> Arrangement.SpaceAround
            VerticalArrangementOption.SpaceEvenly.name -> Arrangement.SpaceEvenly
            else -> Arrangement.spacedBy(spacing.dp)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = hAlign,
            verticalArrangement = vArrangement
        ) {
            if (node.children.isEmpty()) {
                EmptyContainerPlaceholder("Column")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}