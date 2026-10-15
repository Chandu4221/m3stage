package io.github.chandu4221.m3stage.adapter.renderer.component.containment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.definition.IconDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.IconGlyph

class IconRenderer : NodeRenderer {

    companion object {
        fun resolveVector(glyph: IconGlyph): ImageVector = when (glyph) {
            IconGlyph.Home -> Icons.Default.Home
            IconGlyph.Search -> Icons.Default.Search
            IconGlyph.Settings -> Icons.Default.Settings
            IconGlyph.Person -> Icons.Default.Person
            IconGlyph.Add -> Icons.Default.Add
            IconGlyph.Favorite -> Icons.Default.Favorite
            IconGlyph.Star -> Icons.Default.Star
            IconGlyph.Menu -> Icons.Default.Menu
            IconGlyph.ArrowBack -> Icons.Default.ArrowBack
            IconGlyph.MoreVert -> Icons.Default.MoreVert
            IconGlyph.Check -> Icons.Default.Check
            IconGlyph.Close -> Icons.Default.Close
            IconGlyph.Email -> Icons.Default.Email
            IconGlyph.ShoppingCart -> Icons.Default.ShoppingCart
            IconGlyph.Info -> Icons.Default.Info
        }
    }

    @Composable
    override fun Render(
        node: DesignNode,
        modifier: Modifier,
        renderChild: @Composable ((DesignNode) -> Unit)
    ) {
        val glyph = IconDefinition.resolveGlyph(node)
        val tint = ThemeResolver.resolveColor(node[IconDefinition.Tint])

        Icon(
            imageVector = resolveVector(glyph),
            contentDescription = glyph.name,
            tint = if (tint != Color.Unspecified) tint else androidx.compose.material3.LocalContentColor.current,
            modifier = modifier
        )
    }
}
