package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.component.ComponentCatalog.TextFieldProps
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class TextFieldRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val textValue = (node.props[TextFieldProps.Value.id] as? PropertyValue.StringValue)?.value ?: ""
        val label = (node.props[TextFieldProps.Label.id] as? PropertyValue.StringValue)?.value ?: "Label"
        val placeholder = (node.props[TextFieldProps.Placeholder.id] as? PropertyValue.StringValue)?.value ?: ""
        val singleLine = (node.props[TextFieldProps.SingleLine.id] as? PropertyValue.BooleanValue)?.value ?: true

        OutlinedTextField(
            value = textValue,
            onValueChange = {},
            label = if (label.isNotEmpty()) {
                { Text(label) }
            } else null,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder) }
            } else null,
            singleLine = singleLine,
            modifier = modifier
        )
    }
}