package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.ContentAlignmentOption

class BoxRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val alignment = when (node[ComponentCatalog.BoxProps.ContentAlignment]?.name) {
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

        Box(contentAlignment = alignment) {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}