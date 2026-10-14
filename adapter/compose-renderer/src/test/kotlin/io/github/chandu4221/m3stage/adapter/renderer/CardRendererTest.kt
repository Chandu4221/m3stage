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
            props = mapOf(TextDefinition.Text to PropertyValue.StringValue("Title"))
        )
        val text2 = DesignNode(
            id = NodeId("text_2"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.Text to PropertyValue.StringValue("Body"))
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
}
