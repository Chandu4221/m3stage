package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class ScaffoldRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val containerColor = ThemeResolver.resolveColor(node[ScaffoldDefinition.ContainerColor])
        
        // Slot resolution with backward-compatible raw children fallback
        val topBarNode = node.slots[ScaffoldDefinition.TopBarSlot.id]?.firstOrNull()
            ?: node.children.firstOrNull { it.kind == ComponentKind.TopAppBar }
        val bottomBarNode = node.slots[ScaffoldDefinition.BottomBarSlot.id]?.firstOrNull()
        val fabNode = node.slots[ScaffoldDefinition.FloatingActionButtonSlot.id]?.firstOrNull()
        val contentChildren = node.slots[ScaffoldDefinition.ContentSlot.id]
            ?: node.children.filter { it != topBarNode && it != bottomBarNode && it != fabNode }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = if (containerColor != Color.Unspecified) containerColor else MaterialTheme.colorScheme.background,
            topBar = {
                topBarNode?.let { renderChild(it) }
            },
            bottomBar = {
                bottomBarNode?.let { renderChild(it) }
            },
            floatingActionButton = {
                fabNode?.let { renderChild(it) }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (contentChildren.isEmpty() && topBarNode == null && bottomBarNode == null && fabNode == null) {
                    EmptyContainerPlaceholder("Scaffold")
                } else {
                    contentChildren.forEach { child ->
                        renderChild(child)
                    }
                }
            }
        }
    }
}
