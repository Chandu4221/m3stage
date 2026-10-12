package io.github.chandu4221.m3stage.adapter.renderer.component.textinputs

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.definition.OutlinedTextFieldDefinition
import io.github.chandu4221.m3stage.component.definition.TextFieldDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class OutlinedTextFieldRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val value = node[OutlinedTextFieldDefinition.Value]?.value ?: ""
        val label = node[OutlinedTextFieldDefinition.Label]?.value ?: ""
        val placeholder = node[OutlinedTextFieldDefinition.Placeholder]?.value ?: ""
        val isSingleLine = node[OutlinedTextFieldDefinition.SingleLine]?.value ?: true

        val leadingIcon = node.slots[TextFieldDefinition.LeadingIconSlot.id]?.firstOrNull()
        val trailingIcon = node.slots[TextFieldDefinition.TrailingIconSlot.id]?.firstOrNull()
        val supportingText = node.slots[TextFieldDefinition.SupportingTextSlot.id]?.firstOrNull()

        OutlinedTextField(
            value = value,
            onValueChange = {},
            singleLine = isSingleLine,
            label = if (label.isNotBlank()) {
                { Text(label) }
            } else null,
            placeholder = if (placeholder.isNotBlank()) {
                { Text(placeholder) }
            } else null,
            leadingIcon = leadingIcon?.let { { renderChild(it) } },
            trailingIcon = trailingIcon?.let { { renderChild(it) } },
            supportingText = supportingText?.let { { renderChild(it) } },
            modifier = modifier
        )
    }
}
