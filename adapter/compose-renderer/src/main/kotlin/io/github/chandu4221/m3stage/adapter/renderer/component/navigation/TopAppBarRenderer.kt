package io.github.chandu4221.m3stage.adapter.renderer.component.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.TopAppBarDefinition
import io.github.chandu4221.m3stage.model.DesignNode

@OptIn(ExperimentalMaterial3Api::class)
class TopAppBarRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val titleText = node[TopAppBarDefinition.Title]?.value ?: "Title"
        val containerColor = ThemeResolver.resolveColor(node[TopAppBarDefinition.ContainerColor])
        val titleContentColor = ThemeResolver.resolveColor(node[TopAppBarDefinition.TitleContentColor])

        val colors = when {
            containerColor != Color.Unspecified && titleContentColor != Color.Unspecified ->
                TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = titleContentColor
                )
            containerColor != Color.Unspecified ->
                TopAppBarDefaults.topAppBarColors(containerColor = containerColor)
            titleContentColor != Color.Unspecified ->
                TopAppBarDefaults.topAppBarColors(titleContentColor = titleContentColor)
            else ->
                TopAppBarDefaults.topAppBarColors()
        }

        val titleNode = node.slots[TopAppBarDefinition.TitleSlot.id]?.firstOrNull()
        val navIconNode = node.slots[TopAppBarDefinition.NavigationIconSlot.id]?.firstOrNull()
        val actionChildren = node.slots[TopAppBarDefinition.ActionsSlot.id]
            ?: node.children.filter { it.kind == ComponentKind.Icon || it.kind == ComponentKind.Button }

        TopAppBar(
            title = {
                if (titleNode != null) {
                    renderChild(titleNode)
                } else {
                    Text(text = titleText)
                }
            },
            navigationIcon = {
                navIconNode?.let { renderChild(it) }
            },
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
