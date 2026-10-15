package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.ColumnDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.HorizontalAlignmentOption
import io.github.chandu4221.m3stage.property.VerticalArrangementOption

class ColumnRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val spacing = node[ColumnDefinition.Spacing]?.value ?: 0f
        val horizontalAlignmentOption = node[ColumnDefinition.HorizontalAlignment]?.name
        val horizontalAlignment = when (horizontalAlignmentOption) {
            HorizontalAlignmentOption.CenterHorizontally.name -> Alignment.CenterHorizontally
            HorizontalAlignmentOption.End.name -> Alignment.End
            else -> Alignment.Start
        }

        val verticalArrangementOption = node[ColumnDefinition.VerticalArrangement]?.name
        val verticalArrangement = when {
            spacing > 0f -> Arrangement.spacedBy(spacing.dp)
            verticalArrangementOption == VerticalArrangementOption.Center.name -> Arrangement.Center
            verticalArrangementOption == VerticalArrangementOption.Bottom.name -> Arrangement.Bottom
            verticalArrangementOption == VerticalArrangementOption.SpaceBetween.name -> Arrangement.SpaceBetween
            verticalArrangementOption == VerticalArrangementOption.SpaceAround.name -> Arrangement.SpaceAround
            verticalArrangementOption == VerticalArrangementOption.SpaceEvenly.name -> Arrangement.SpaceEvenly
            else -> Arrangement.Top
        }
        val children = node.allDirectChildren

        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
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
