package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.FloatingActionButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.FabVariant
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FloatingActionButtonRendererTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFabVariantsResolveToExpectedSpecColorTokens() {
        val scheme = lightColorScheme()

        val primaryNode = DesignNode(
            id = NodeId("fab-p"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(FabVariant.Primary.name))
        )
        val secondaryNode = DesignNode(
            id = NodeId("fab-s"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(FabVariant.Secondary.name))
        )
        val tertiaryNode = DesignNode(
            id = NodeId("fab-t"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(FabVariant.Tertiary.name))
        )

        // Verify variant resolution
        assertEquals(FabVariant.Primary, FloatingActionButtonDefinition.resolveVariant(primaryNode))
        assertEquals(FabVariant.Secondary, FloatingActionButtonDefinition.resolveVariant(secondaryNode))
        assertEquals(FabVariant.Tertiary, FloatingActionButtonDefinition.resolveVariant(tertiaryNode))

        // Check tokens
        assertEquals(scheme.primaryContainer, ThemeResolver.resolveColorToken(M3ColorToken.PrimaryContainer, scheme))
        assertEquals(scheme.secondaryContainer, ThemeResolver.resolveColorToken(M3ColorToken.SecondaryContainer, scheme))
        assertEquals(scheme.tertiaryContainer, ThemeResolver.resolveColorToken(M3ColorToken.TertiaryContainer, scheme))
    }

    @Test
    fun testAllFabVariantsComposeSuccessfully() {
        val primaryFab = DesignNode(
            id = NodeId("fab_1"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(FabVariant.Primary.name))
        )
        val secondaryFab = DesignNode(
            id = NodeId("fab_2"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(FabVariant.Secondary.name))
        )
        val tertiaryFab = DesignNode(
            id = NodeId("fab_3"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(FabVariant.Tertiary.name))
        )
        val container = DesignNode(
            id = NodeId("container_col"),
            kind = ComponentKind.Column,
            children = listOf(primaryFab, secondaryFab, tertiaryFab)
        )

        val registry = NodeBoundsRegistry()
        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(400.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = container,
                        selectedNodeId = null,
                        onNodeClick = {},
                        registry = registry
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        assertNotNull(registry.get(primaryFab.id), "Primary FAB must compose and have registered bounds")
        assertNotNull(registry.get(secondaryFab.id), "Secondary FAB must compose and have registered bounds")
        assertNotNull(registry.get(tertiaryFab.id), "Tertiary FAB must compose and have registered bounds")
    }
}
