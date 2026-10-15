package io.github.chandu4221.m3stage.adapter.renderer.component.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.NavigationBarItemDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class NavigationBarItemRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val selected = node[NavigationBarItemDefinition.Selected]?.value ?: false
        val enabled = node[NavigationBarItemDefinition.Enabled]?.value ?: true
        val indicatorColor = ThemeResolver.resolveColor(node[NavigationBarItemDefinition.IndicatorColor])
        val iconNodes = node.slots[NavigationBarItemDefinition.IconSlot.id] ?: emptyList()
        val labelNodes = node.slots[NavigationBarItemDefinition.LabelSlot.id] ?: emptyList()

        val isRail = LocalNavigationRailActive.current
        if (isRail) {
            val colors = if (indicatorColor != Color.Unspecified) {
                NavigationRailItemDefaults.colors(indicatorColor = indicatorColor)
            } else {
                NavigationRailItemDefaults.colors()
            }

            NavigationRailItem(
                selected = selected,
                onClick = {},
                enabled = enabled,
                modifier = modifier,
                colors = colors,
                icon = {
                    if (iconNodes.isNotEmpty()) {
                        iconNodes.forEach { renderChild(it) }
                    } else {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null)
                    }
                },
                label = if (labelNodes.isNotEmpty()) {
                    {
                        labelNodes.forEach { renderChild(it) }
                    }
                } else null
            )
        } else {
            val colors = if (indicatorColor != Color.Unspecified) {
                NavigationBarItemDefaults.colors(indicatorColor = indicatorColor)
            } else {
                NavigationBarItemDefaults.colors()
            }

            @Composable
            fun RowScope.RenderItem() {
                NavigationBarItem(
                    selected = selected,
                    onClick = {},
                    enabled = enabled,
                    modifier = modifier,
                    colors = colors,
                    icon = {
                        if (iconNodes.isNotEmpty()) {
                            iconNodes.forEach { renderChild(it) }
                        } else {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null)
                        }
                    },
                    label = if (labelNodes.isNotEmpty()) {
                        {
                            labelNodes.forEach { renderChild(it) }
                        }
                    } else null
                )
            }

            val rowScope = LocalRowScope.current
            if (rowScope != null) {
                with(rowScope) {
                    RenderItem()
                }
            } else {
                Row {
                    RenderItem()
                }
            }
        }
    }
}

