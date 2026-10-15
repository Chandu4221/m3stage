package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class CardRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val elevation = node[CardDefinition.Elevation]?.value ?: 0f
        val shape = ThemeResolver.resolveShape(node[CardDefinition.Shape])
        val resolvedColor = ThemeResolver.resolveColor(node[CardDefinition.ContainerColor])
        val children = node.allDirectChildren

        val cardColors = if (resolvedColor != Color.Unspecified) {
            CardDefaults.cardColors(containerColor = resolvedColor)
        } else {
            CardDefaults.cardColors()
        }

        Card(
            modifier = modifier,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            shape = shape
        ) {
            if (children.isEmpty()) {
                EmptyContainerPlaceholder("Card")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
