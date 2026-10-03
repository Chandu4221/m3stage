package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode


/**
 * Renders a Button component.
 * Button is a container - it renders its children inside the button lambda.
 */
class ButtonRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val enabled = node.props["enabled"]?.toBoolean() ?: true

        Button(
            onClick = { /* No-op in preview mode */ },
            enabled = enabled
        ) {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}