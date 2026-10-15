package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.model.CardVariant
import io.github.chandu4221.m3stage.model.DesignNode

class CardRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val variant = CardDefinition.resolveVariant(node)
        val elevation = node[CardDefinition.Elevation]?.value ?: 0f
        val shape = ThemeResolver.resolveShape(node[CardDefinition.Shape])
        val resolvedColor = ThemeResolver.resolveColor(node[CardDefinition.ContainerColor])
        val children = node.allDirectChildren

        val content: @Composable ColumnScope.() -> Unit = {
            if (children.isEmpty()) {
                EmptyContainerPlaceholder("Card")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }

        when (variant) {
            CardVariant.Elevated -> {
                val cardColors = if (resolvedColor != Color.Unspecified) {
                    CardDefaults.elevatedCardColors(containerColor = resolvedColor)
                } else {
                    CardDefaults.elevatedCardColors()
                }
                val cardElevation = if (elevation > 0f) {
                    CardDefaults.elevatedCardElevation(defaultElevation = elevation.dp)
                } else {
                    CardDefaults.elevatedCardElevation()
                }
                ElevatedCard(
                    modifier = modifier,
                    colors = cardColors,
                    elevation = cardElevation,
                    shape = shape,
                    content = content
                )
            }
            CardVariant.Filled -> {
                val cardColors = if (resolvedColor != Color.Unspecified) {
                    CardDefaults.cardColors(containerColor = resolvedColor)
                } else {
                    CardDefaults.cardColors()
                }
                val cardElevation = if (elevation > 0f) {
                    CardDefaults.cardElevation(defaultElevation = elevation.dp)
                } else {
                    CardDefaults.cardElevation()
                }
                Card(
                    modifier = modifier,
                    colors = cardColors,
                    elevation = cardElevation,
                    shape = shape,
                    content = content
                )
            }
            CardVariant.Outlined -> {
                val cardColors = if (resolvedColor != Color.Unspecified) {
                    CardDefaults.outlinedCardColors(containerColor = resolvedColor)
                } else {
                    CardDefaults.outlinedCardColors()
                }
                val cardElevation = if (elevation > 0f) {
                    CardDefaults.outlinedCardElevation(defaultElevation = elevation.dp)
                } else {
                    CardDefaults.outlinedCardElevation()
                }
                val resolvedBorderColor = ThemeResolver.resolveColor(node[CardDefinition.BorderColor])
                val borderWidth = node[CardDefinition.BorderWidth]?.value ?: 1f
                val border = if (resolvedBorderColor != Color.Unspecified) {
                    BorderStroke(borderWidth.dp, resolvedBorderColor)
                } else {
                    CardDefaults.outlinedCardBorder()
                }
                OutlinedCard(
                    modifier = modifier,
                    colors = cardColors,
                    elevation = cardElevation,
                    shape = shape,
                    border = border,
                    content = content
                )
            }
        }
    }
}
