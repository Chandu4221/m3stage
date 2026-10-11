package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.IconDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class IconRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val tint = ThemeResolver.resolveColor(node[IconDefinition.Tint])

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = if (tint != Color.Unspecified) tint else androidx.compose.material3.LocalContentColor.current,
            modifier = modifier
        )
    }
}
