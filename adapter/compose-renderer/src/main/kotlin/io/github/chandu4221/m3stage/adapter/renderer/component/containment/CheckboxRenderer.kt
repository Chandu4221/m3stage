package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.CheckboxDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class CheckboxRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val checked = node[CheckboxDefinition.Checked]?.value ?: false
        val enabled = node[CheckboxDefinition.Enabled]?.value ?: true

        Checkbox(
            checked = checked,
            onCheckedChange = {},
            enabled = enabled,
            modifier = modifier
        )
    }
}
