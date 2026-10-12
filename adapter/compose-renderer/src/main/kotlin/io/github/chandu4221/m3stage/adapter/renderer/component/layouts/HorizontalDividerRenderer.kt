package io.github.chandu4221.m3stage.adapter.renderer.component.layouts

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.HorizontalDividerDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class HorizontalDividerRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val thickness = node[HorizontalDividerDefinition.Thickness]?.value ?: 1f
        val color = ThemeResolver.resolveColor(node[HorizontalDividerDefinition.Color])

        HorizontalDivider(
            modifier = modifier,
            thickness = thickness.dp,
            color = color ?: androidx.compose.material3.DividerDefaults.color
        )
    }
}
