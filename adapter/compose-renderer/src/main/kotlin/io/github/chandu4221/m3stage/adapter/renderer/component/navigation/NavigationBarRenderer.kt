package io.github.chandu4221.m3stage.adapter.renderer.component.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.LocalIsAdaptiveScreen
import io.github.chandu4221.m3stage.adapter.renderer.LocalWindowSizeClass
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.NavigationBarDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.WindowSizeClass

val LocalRowScope = compositionLocalOf<RowScope?> { null }
val LocalNavigationRailActive = compositionLocalOf { false }

class NavigationBarRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val containerColor = ThemeResolver.resolveColor(node[NavigationBarDefinition.ContainerColor])
        val children = node.allDirectChildren
        val isAdaptive = LocalIsAdaptiveScreen.current
        val windowSizeClass = LocalWindowSizeClass.current

        if (isAdaptive && windowSizeClass == WindowSizeClass.Expanded) {
            NavigationRail(
                modifier = modifier,
                containerColor = if (containerColor != Color.Unspecified) containerColor else NavigationRailDefaults.ContainerColor
            ) {
                if (children.isEmpty()) {
                    EmptyContainerPlaceholder("NavigationRail")
                } else {
                    CompositionLocalProvider(LocalNavigationRailActive provides true) {
                        children.forEach { child ->
                            renderChild(child)
                        }
                    }
                }
            }
        } else {
            NavigationBar(
                modifier = modifier,
                containerColor = if (containerColor != Color.Unspecified) containerColor else NavigationBarDefaults.containerColor
            ) {
                if (children.isEmpty()) {
                    EmptyContainerPlaceholder("NavigationBar")
                } else {
                    CompositionLocalProvider(LocalRowScope provides this) {
                        children.forEach { child ->
                            renderChild(child)
                        }
                    }
                }
            }
        }
    }
}

