package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.SwitchDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class SwitchRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val checked = node[SwitchDefinition.Checked]?.value ?: true
        val enabled = node[SwitchDefinition.Enabled]?.value ?: true

        Switch(
            checked = checked,
            onCheckedChange = {},
            enabled = enabled,
            modifier = modifier
        )
    }
}
