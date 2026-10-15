package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.property.PropertyValue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CardRendererTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCardWrapsContentInsteadOfFillingContainerHeight() {
        val text1 = DesignNode(
            id = NodeId("text_1"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Title"))
        )
        val text2 = DesignNode(
            id = NodeId("text_2"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Body"))
        )
        val card = DesignNode(
            id = NodeId("card_node"),
            kind = ComponentKind.Card,
            children = listOf(text1, text2)
        )
        val containerColumn = DesignNode(
            id = NodeId("column_node"),
            kind = ComponentKind.Column,
            children = listOf(card)
        )

        val registry = NodeBoundsRegistry()

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(400.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = containerColumn,
                        selectedNodeId = null,
                        onNodeClick = {},
                        registry = registry
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        val cardBounds = registry.get(card.id)
        val columnBounds = registry.get(containerColumn.id)

        assertNotNull(cardBounds, "Card bounds should be registered")
        assertNotNull(columnBounds, "Column bounds should be registered")

        // Card height must wrap content and be significantly less than the 800dp (or ~800px) container height
        assertTrue(
            cardBounds.height < 400f,
            "Card should wrap content instead of filling available height, but measured height was ${cardBounds.height}"
        )
        assertTrue(
            cardBounds.height > 0f,
            "Card should have non-zero height, but measured height was ${cardBounds.height}"
        )
    }

    @Test
    fun testCardUnsetContainerColorResolvesToUnspecified() {
        val defaultCard = io.github.chandu4221.m3stage.component.ComponentCatalog[ComponentKind.Card].createDefaultSubtree(
            object : io.github.chandu4221.m3stage.port.IdGenerator {
                override fun nextProjectId() = io.github.chandu4221.m3stage.model.ProjectId("p1")
                override fun nextScreenId() = io.github.chandu4221.m3stage.model.ScreenId("s1")
                override fun nextNodeId() = NodeId("card_default")
            }
        )
        val scheme = lightColorScheme()
        val resolved = io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver.resolveColor(
            defaultCard[CardDefinition.ContainerColor],
            scheme
        )
        kotlin.test.assertEquals(
            androidx.compose.ui.graphics.Color.Unspecified,
            resolved,
            "Unset Card container color must resolve to Color.Unspecified to allow CardDefaults spec inheritance"
        )
    }

    @Test
    fun testCardExplicitContainerColorResolvesToThemeToken() {
        val card = DesignNode(
            id = NodeId("card_explicit"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.ContainerColor.id to PropertyValue.ColorValue.Token(io.github.chandu4221.m3stage.theme.M3ColorToken.PrimaryContainer)
            )
        )
        val scheme = lightColorScheme()
        val resolved = io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver.resolveColor(
            card[CardDefinition.ContainerColor],
            scheme
        )
        kotlin.test.assertEquals(
            scheme.primaryContainer,
            resolved,
            "Explicit Card container color token must resolve to color scheme's matching token"
        )
    }

    @Test
    fun testCardComposesSuccessfullyWithUnsetAndExplicitColor() {
        val unsetCard = DesignNode(
            id = NodeId("card_unset"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.ContainerColor.id to PropertyValue.ColorValue.Token(io.github.chandu4221.m3stage.theme.M3ColorToken.Inherit)
            )
        )
        val explicitCard = DesignNode(
            id = NodeId("card_explicit"),
            kind = ComponentKind.Card,
            props = mapOf(
                CardDefinition.ContainerColor.id to PropertyValue.ColorValue.Token(io.github.chandu4221.m3stage.theme.M3ColorToken.PrimaryContainer)
            )
        )
        val container = DesignNode(
            id = NodeId("container_col"),
            kind = ComponentKind.Column,
            children = listOf(unsetCard, explicitCard)
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

        assertNotNull(registry.get(unsetCard.id), "Unset card must compose and have registered bounds")
        assertNotNull(registry.get(explicitCard.id), "Explicit card must compose and have registered bounds")
    }
}
