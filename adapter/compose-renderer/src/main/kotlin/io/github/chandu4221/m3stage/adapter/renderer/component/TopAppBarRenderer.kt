package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

@OptIn(ExperimentalMaterial3Api::class)
class TopAppBarRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val titleText = node[ComponentCatalog.TopAppBarProps.Title]?.value ?: "Title"
        val containerColor = ThemeResolver.resolveColor(node[ComponentCatalog.TopAppBarProps.ContainerColor])
        val titleContentColor = ThemeResolver.resolveColor(node[ComponentCatalog.TopAppBarProps.TitleContentColor])

        val colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            titleContentColor = titleContentColor
        )

        val actionChildren = node.children.filter { it.kind == ComponentKind.Icon || it.kind == ComponentKind.Button }

        TopAppBar(
            title = { Text(text = titleText) },
            actions = {
                actionChildren.forEach { child ->
                    renderChild(child)
                }
            },
            colors = colors,
            modifier = modifier
        )
    }
}