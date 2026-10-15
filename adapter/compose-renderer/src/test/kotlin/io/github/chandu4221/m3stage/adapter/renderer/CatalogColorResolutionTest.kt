package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CatalogColorResolutionTest {

    private val scheme = lightColorScheme()

    @Test
    fun testEveryCatalogColorDefaultResolvesToUnspecified() {
        for (def in ComponentCatalog.all) {
            val colorSpecs = def.properties.filterIsInstance<PropertySpec.ColorPicker>()
            for (spec in colorSpecs) {
                val resolved = ThemeResolver.resolveColor(spec.defaultValue, scheme)
                assertEquals(
                    Color.Unspecified,
                    resolved,
                    "Component '${def.kind.name}' property '${spec.displayName}' default color must resolve to Color.Unspecified"
                )
            }
        }
    }

    @Test
    fun testNullOrUnsetColorResolvesToUnspecified() {
        assertEquals(Color.Unspecified, ThemeResolver.resolveColor(null, scheme))
        assertEquals(
            Color.Unspecified,
            ThemeResolver.resolveColor(PropertyValue.ColorValue.Token(M3ColorToken.Inherit), scheme)
        )
    }

    @Test
    fun testExplicitTokensResolveToNonUnspecifiedColors() {
        val nonInheritTokens = M3ColorToken.entries.filter { it != M3ColorToken.Inherit }
        for (token in nonInheritTokens) {
            val resolved = ThemeResolver.resolveColor(PropertyValue.ColorValue.Token(token), scheme)
            assertNotEquals(
                Color.Unspecified,
                resolved,
                "Token $token must resolve to a valid non-Unspecified Compose Color"
            )
        }
    }
}
