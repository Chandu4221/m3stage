package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.BoxDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.ContentAlignmentOption

class BoxRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val alignmentOption = node[BoxDefinition.ContentAlignment]?.name
        val contentAlignment = when (alignmentOption) {
            ContentAlignmentOption.TopStart.name -> Alignment.TopStart
            ContentAlignmentOption.TopCenter.name -> Alignment.TopCenter
            ContentAlignmentOption.TopEnd.name -> Alignment.TopEnd
            ContentAlignmentOption.CenterStart.name -> Alignment.CenterStart
            ContentAlignmentOption.Center.name -> Alignment.Center
            ContentAlignmentOption.CenterEnd.name -> Alignment.CenterEnd
            ContentAlignmentOption.BottomStart.name -> Alignment.BottomStart
            ContentAlignmentOption.BottomCenter.name -> Alignment.BottomCenter
            ContentAlignmentOption.BottomEnd.name -> Alignment.BottomEnd
            else -> Alignment.TopStart
        }
        val children = node.allDirectChildren
        Box(
            modifier = modifier,
            contentAlignment = contentAlignment
        ) {
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
