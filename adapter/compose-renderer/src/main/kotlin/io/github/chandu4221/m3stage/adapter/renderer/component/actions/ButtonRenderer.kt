package io.github.chandu4221.m3stage.adapter.renderer.component.actions

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.ButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class ButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val enabled = node[ButtonDefinition.Enabled]?.value ?: true
        val containerColor = ThemeResolver.resolveColor(node[ButtonDefinition.ContainerColor])
        val contentColor = ThemeResolver.resolveColor(node[ButtonDefinition.ContentColor])

        val colors = when {
            containerColor != Color.Unspecified && contentColor != Color.Unspecified ->
                ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
            containerColor != Color.Unspecified ->
                ButtonDefaults.buttonColors(containerColor = containerColor)
            contentColor != Color.Unspecified ->
                ButtonDefaults.buttonColors(contentColor = contentColor)
            else ->
                ButtonDefaults.buttonColors()
        }

        val leadingIcon = node.slots[ButtonDefinition.LeadingIconSlot.id]?.firstOrNull()
        val label = node.slots[ButtonDefinition.LabelSlot.id]?.firstOrNull()
        val trailingIcon = node.slots[ButtonDefinition.TrailingIconSlot.id]?.firstOrNull()

        Button(
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
                Text(text = "Button")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
