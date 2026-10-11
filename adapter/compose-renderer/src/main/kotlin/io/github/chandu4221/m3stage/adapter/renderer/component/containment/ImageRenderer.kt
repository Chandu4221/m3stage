package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.ImageDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class ImageRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val contentDescription = node[ImageDefinition.ContentDescription]?.value

        Image(
            imageVector = Icons.Default.Place,
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}
