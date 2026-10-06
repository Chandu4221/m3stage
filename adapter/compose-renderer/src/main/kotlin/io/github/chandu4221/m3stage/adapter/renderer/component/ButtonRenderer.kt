package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode

class ButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val enabled = node[ComponentCatalog.ButtonProps.Enabled]?.value ?: true
        val containerColor = ThemeResolver.resolveColor(node[ComponentCatalog.ButtonProps.ContainerColor])
        val contentColor = ThemeResolver.resolveColor(node[ComponentCatalog.ButtonProps.ContentColor])

        val colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )

        Button(
            onClick = {},
            enabled = enabled,
            colors = colors,
            modifier = Modifier.padding(4.dp)
        ) {
            if (node.children.isEmpty()) {
                Text(text = "Button")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}