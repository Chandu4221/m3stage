package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.RadioButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class RadioButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val selected = node[RadioButtonDefinition.Selected]?.value ?: false
        val enabled = node[RadioButtonDefinition.Enabled]?.value ?: true

        RadioButton(
            selected = selected,
            onClick = {},
            enabled = enabled,
            modifier = modifier
        )
    }
}
