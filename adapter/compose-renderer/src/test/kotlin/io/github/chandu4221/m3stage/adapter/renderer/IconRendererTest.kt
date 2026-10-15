package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.component.containment.IconRenderer
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.IconDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.IconGlyph
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IconRendererTest {

    @Test
    fun testEveryIconGlyphResolvesToNonNullImageVector() {
        for (glyph in IconGlyph.entries) {
            val vector = IconRenderer.resolveVector(glyph)
            assertNotNull(vector, "Glyph $glyph must map to a non-null ImageVector")
            assertEquals(glyph.name, glyph.name)
        }
    }

    @Test
    fun testIconDefinitionResolvesGlyphProperty() {
        for (glyph in IconGlyph.entries) {
            val node = DesignNode(
                id = NodeId("test_icon_${glyph.name}"),
                kind = ComponentKind.Icon,
                props = mapOf(
                    IconDefinition.Glyph.id to PropertyValue.EnumValue(glyph.name)
                )
            )
            assertEquals(glyph, IconDefinition.resolveGlyph(node))
        }
    }

    @Test
    fun testIconDefinitionFallsBackToIconNameForBackwardCompatibility() {
        val node = DesignNode(
            id = NodeId("test_icon_legacy"),
            kind = ComponentKind.Icon,
            props = mapOf(
                IconDefinition.IconName.id to PropertyValue.StringValue("Search")
            )
        )
        assertEquals(IconGlyph.Search, IconDefinition.resolveGlyph(node))
    }

    @Test
    fun testIconDefinitionDefaultFallbackIsAdd() {
        val node = DesignNode(
            id = NodeId("test_icon_empty"),
            kind = ComponentKind.Icon
        )
        assertEquals(IconGlyph.Add, IconDefinition.resolveGlyph(node))
    }

    @Test
    fun testThemeResolverMapsInheritColorTokenToUnspecified() {
        val scheme = ThemeResolver.resolveScheme(0xFF6750A4L, false)
        val resolved = ThemeResolver.resolveColorToken(M3ColorToken.Inherit, scheme)
        assertEquals(Color.Unspecified, resolved, "Inherit token must resolve to Color.Unspecified")
    }
}
