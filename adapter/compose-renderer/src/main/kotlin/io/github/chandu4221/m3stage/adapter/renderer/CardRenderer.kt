package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DesignNode


class CardRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val elevation = node.props["elevation"]?.toFloatOrNull() ?: 1f

        Card(elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)) {
            node.children.forEach { child ->
                renderChild(child)
            }
        }
    }
}