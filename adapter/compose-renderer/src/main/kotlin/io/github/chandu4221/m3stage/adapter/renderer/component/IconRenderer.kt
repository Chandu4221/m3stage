package io.github.chandu4221.m3stage.adapter.renderer.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog.IconProps
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken

class IconRenderer : NodeRenderer {
    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable (DesignNode) -> Unit
    ) {
        val iconName = (node.props[IconProps.IconName.id] as? PropertyValue.StringValue)?.value ?: "Star"
        val tintToken =
            (node.props[IconProps.Tint.id] as? PropertyValue.ColorValue.Token)?.token ?: M3ColorToken.Primary
        val tint = ThemeResolver.resolveColorToken(tintToken)

        val imageVector = when (iconName.lowercase()) {
            "star" -> Icons.Default.Star
            "favorite", "heart" -> Icons.Default.Favorite
            "settings" -> Icons.Default.Settings
            "add", "plus" -> Icons.Default.Add
            "home" -> Icons.Default.Home
            "person", "user" -> Icons.Default.Person
            "search" -> Icons.Default.Search
            "close", "clear" -> Icons.Default.Close
            "check" -> Icons.Default.Check
            else -> Icons.Default.Star
        }

        Icon(
            imageVector = imageVector,
            contentDescription = iconName,
            tint = tint,
            modifier = modifier
        )
    }

}