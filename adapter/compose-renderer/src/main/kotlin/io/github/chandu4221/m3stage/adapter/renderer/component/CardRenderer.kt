package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode

class CardRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val elevation = node[ComponentCatalog.CardProps.Elevation]?.value ?: 1f
        val containerColor = ThemeResolver.resolveColor(node[ComponentCatalog.CardProps.ContainerColor])
        val shape = ThemeResolver.resolveShape(node[ComponentCatalog.CardProps.Shape])

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = shape,
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            if (node.children.isEmpty()) {
                EmptyContainerPlaceholder("Card")
            } else {
                node.children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}