package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.RowDefinition
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
        val spacing = node[RowDefinition.Spacing]?.value ?: 0f
        val verticalAlignmentOption = node[RowDefinition.VerticalAlignment]?.name
        val verticalAlignment = when (verticalAlignmentOption) {
            VerticalAlignmentOption.CenterVertically.name -> Alignment.CenterVertically
            VerticalAlignmentOption.Bottom.name -> Alignment.Bottom
            else -> Alignment.Top
        }

        val horizontalArrangementOption = node[RowDefinition.HorizontalArrangement]?.name
        val horizontalArrangement = when {
            spacing > 0f -> Arrangement.spacedBy(spacing.dp)
            horizontalArrangementOption == HorizontalArrangementOption.Center.name -> Arrangement.Center
            horizontalArrangementOption == HorizontalArrangementOption.End.name -> Arrangement.End
            horizontalArrangementOption == HorizontalArrangementOption.SpaceBetween.name -> Arrangement.SpaceBetween
            horizontalArrangementOption == HorizontalArrangementOption.SpaceAround.name -> Arrangement.SpaceAround
            horizontalArrangementOption == HorizontalArrangementOption.SpaceEvenly.name -> Arrangement.SpaceEvenly
            else -> Arrangement.Start
        }
        val children = node.allDirectChildren

        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment
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
