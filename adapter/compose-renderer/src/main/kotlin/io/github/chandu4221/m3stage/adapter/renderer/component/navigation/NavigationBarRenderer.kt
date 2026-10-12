package io.github.chandu4221.m3stage.adapter.renderer.component.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.NavigationBarDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class NavigationBarRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val containerColor = ThemeResolver.resolveColor(node[NavigationBarDefinition.ContainerColor])
        val children = node.allDirectChildren

        NavigationBar(
            modifier = modifier,
            containerColor = containerColor ?: NavigationBarDefaults.containerColor
        ) {
            if (children.isEmpty()) {
                EmptyContainerPlaceholder("NavigationBar")
            } else {
                children.forEach { child ->
                    renderChild(child)
                }
            }
        }
    }
}
