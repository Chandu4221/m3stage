package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.EmptyContainerPlaceholder
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class ScaffoldRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val containerColor = ThemeResolver.resolveColor(node[ComponentCatalog.ScaffoldProps.ContainerColor])
        val topBarNode = node.children.firstOrNull { it.kind == ComponentKind.TopAppBar }
        val contentChildren = node.children.filter { it != topBarNode }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = containerColor,
            topBar = {
                topBarNode?.let { renderChild(it) }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (contentChildren.isEmpty() && topBarNode == null) {
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