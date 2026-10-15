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

class PrefilledPlaceholderRendererTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPrefilledNodeRendersWithValidBounds() {
        val prefilledText = DesignNode(
            id = NodeId("prefilled_text"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Placeholder Content")),
            isPrefilled = true
        )
        val authoredText = DesignNode(
            id = NodeId("authored_text"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Authored Content")),
            isPrefilled = false
        )
        val column = DesignNode(
            id = NodeId("col"),
            kind = ComponentKind.Column,
            children = listOf(prefilledText, authoredText)
        )

        val registry = NodeBoundsRegistry()

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(400.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = column,
                        selectedNodeId = null,
                        onNodeClick = {},
                        registry = registry
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        val prefilledRect = registry.get(NodeId("prefilled_text"))
        val authoredRect = registry.get(NodeId("authored_text"))

        assertNotNull(prefilledRect, "Prefilled node must be registered in NodeBoundsRegistry")
        assertNotNull(authoredRect, "Authored node must be registered in NodeBoundsRegistry")
        assertTrue(prefilledRect.width > 0f && prefilledRect.height > 0f, "Prefilled bounds must be non-zero")
        assertTrue(authoredRect.width > 0f && authoredRect.height > 0f, "Authored bounds must be non-zero")
    }

    @Test
    fun testPrefilledButtonRendersWithoutOpacityCascade() {
        val buttonLabel = DesignNode(
            id = NodeId("btn_label"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Click Me")),
            isPrefilled = true
        )
        val button = DesignNode(
            id = NodeId("prefilled_btn"),
            kind = ComponentKind.Button,
            slots = mapOf(io.github.chandu4221.m3stage.component.slot.SlotId("label") to listOf(buttonLabel)),
            isPrefilled = true
        )
        val column = DesignNode(
            id = NodeId("root_col"),
            kind = ComponentKind.Column,
            children = listOf(button),
            isPrefilled = true
        )

        val registry = NodeBoundsRegistry()

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(400.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = column,
                        selectedNodeId = null,
                        onNodeClick = {},
                        registry = registry
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        val btnBounds = registry.get(NodeId("prefilled_btn"))
        val labelBounds = registry.get(NodeId("btn_label"))
        assertNotNull(btnBounds, "Button bounds must be registered")
        assertNotNull(labelBounds, "Button label bounds must be registered")
        assertTrue(btnBounds.width > 0f && btnBounds.height > 0f)
        assertTrue(labelBounds.width > 0f && labelBounds.height > 0f)
    }
}
