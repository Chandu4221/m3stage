package io.github.chandu4221.m3stage.adapter.renderer.component.textinputs

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.TextFieldDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class TextFieldRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val value = node[TextFieldDefinition.Value]?.value ?: ""
        val label = node[TextFieldDefinition.Label]?.value ?: ""
        val placeholder = node[TextFieldDefinition.Placeholder]?.value ?: ""
        val singleLine = node[TextFieldDefinition.SingleLine]?.value ?: true

        val leadingIcon = node.slots[TextFieldDefinition.LeadingIconSlot.id]?.firstOrNull()
        val trailingIcon = node.slots[TextFieldDefinition.TrailingIconSlot.id]?.firstOrNull()
        val supportingText = node.slots[TextFieldDefinition.SupportingTextSlot.id]?.firstOrNull()

        TextField(
            value = value,
            onValueChange = {},
            label = if (label.isNotBlank()) {
                { Text(label) }
            } else null,
            placeholder = if (placeholder.isNotBlank()) {
                { Text(placeholder) }
            } else null,
            leadingIcon = leadingIcon?.let { { renderChild(it) } },
            trailingIcon = trailingIcon?.let { { renderChild(it) } },
            supportingText = supportingText?.let { { renderChild(it) } },
            singleLine = singleLine,
            modifier = modifier
        )
    }
}
