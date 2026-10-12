package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.SliderDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class SliderRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val value = node[SliderDefinition.Value]?.value ?: 50f
        val enabled = node[SliderDefinition.Enabled]?.value ?: true

        Slider(
            value = value,
            onValueChange = {},
            valueRange = 0f..100f,
            enabled = enabled,
            modifier = modifier
        )
    }
}
