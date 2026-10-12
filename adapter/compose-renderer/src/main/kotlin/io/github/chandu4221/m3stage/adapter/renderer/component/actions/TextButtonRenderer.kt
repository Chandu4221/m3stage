package io.github.chandu4221.m3stage.adapter.renderer.component.actions

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.ButtonDefinition
import io.github.chandu4221.m3stage.component.definition.TextButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class TextButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val enabled = node[TextButtonDefinition.Enabled]?.value ?: true
        val contentColor = ThemeResolver.resolveColor(node[TextButtonDefinition.ContentColor])

        val colors = if (contentColor != null) {
            ButtonDefaults.textButtonColors(contentColor = contentColor)
        } else {
            ButtonDefaults.textButtonColors()
        }

        val leadingIcon = node.slots[ButtonDefinition.LeadingIconSlot.id]?.firstOrNull()
        val label = node.slots[TextButtonDefinition.LabelSlot.id]?.firstOrNull()
        val trailingIcon = node.slots[ButtonDefinition.TrailingIconSlot.id]?.firstOrNull()

        TextButton(
            onClick = {},
            enabled = enabled,
            colors = colors,
            modifier = modifier.padding(4.dp)
        ) {
            if (leadingIcon != null || label != null || trailingIcon != null) {
                leadingIcon?.let { renderChild(it) }
                label?.let { renderChild(it) }
                trailingIcon?.let { renderChild(it) }
            } else if (node.children.isEmpty()) {
                Text(text = "Text Button")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
