package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.component.containment.IconRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.navigation.NavigationBarItemRenderer
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.IconDefinition
import io.github.chandu4221.m3stage.component.definition.NavigationBarItemDefinition
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.property.PropertyValue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class NavigationBarItemSpacingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFallbackNavigationBarItemsHaveEqualWidthWithoutShrinkWrapping() {
        val renderer = NavigationBarItemRenderer()
        val iconRenderer = IconRenderer()

        // Full item with icon and label
        val fullItem = DesignNode(
            id = NodeId("item-0"),
            kind = ComponentKind.NavigationBarItem,
            props = mapOf(NavigationBarItemDefinition.Selected.id to PropertyValue.BooleanValue(true)),
            slots = mapOf(
                NavigationBarItemDefinition.IconSlot.id to listOf(
                    DesignNode(
                        id = NodeId("icon-0"),
                        kind = ComponentKind.Icon,
                        props = mapOf(IconDefinition.Glyph.id to PropertyValue.EnumValue("Home"))
                    )
                ),
                NavigationBarItemDefinition.LabelSlot.id to listOf(
                    DesignNode(
                        id = NodeId("label-0"),
                        kind = ComponentKind.Text,
                        props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Home Label"))
                    )
                )
            )
        )

        // Empty items (no icon, no label - purely fallback)
        val emptyItem1 = DesignNode(id = NodeId("item-1"), kind = ComponentKind.NavigationBarItem)
        val emptyItem2 = DesignNode(id = NodeId("item-2"), kind = ComponentKind.NavigationBarItem)
        val emptyItem3 = DesignNode(id = NodeId("item-3"), kind = ComponentKind.NavigationBarItem)

        val items = listOf(fullItem, emptyItem1, emptyItem2, emptyItem3)

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                // Outer container without LocalRowScope
                Row(modifier = Modifier.requiredWidth(800.dp)) {
                    items.forEachIndexed { index, item ->
                        Box(modifier = Modifier.weight(1f)) {
                            renderer.Render(
                                node = item,
                                modifier = Modifier.testTag("item-$index")
                            ) { child ->
                                when (child.kind) {
                                    ComponentKind.Icon -> iconRenderer.Render(child, Modifier) {}
                                    ComponentKind.Text -> Text(child[TextDefinition.TextContent]?.value ?: "")
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()

        val bounds0 = composeTestRule.onNodeWithTag("item-0").getUnclippedBoundsInRoot()
        val bounds1 = composeTestRule.onNodeWithTag("item-1").getUnclippedBoundsInRoot()
        val bounds2 = composeTestRule.onNodeWithTag("item-2").getUnclippedBoundsInRoot()
        val bounds3 = composeTestRule.onNodeWithTag("item-3").getUnclippedBoundsInRoot()

        val width0 = bounds0.right - bounds0.left
        val width1 = bounds1.right - bounds1.left
        val width2 = bounds2.right - bounds2.left
        val width3 = bounds3.right - bounds3.left

        // All 4 items must share the 800.dp width equally (200.dp each)
        assertEquals(200.dp.value, width0.value, 0.5f, "Full item width must equal 200.dp")
        assertEquals(200.dp.value, width1.value, 0.5f, "Empty item 1 width must equal 200.dp (no shrink-wrapping)")
        assertEquals(200.dp.value, width2.value, 0.5f, "Empty item 2 width must equal 200.dp (no shrink-wrapping)")
        assertEquals(200.dp.value, width3.value, 0.5f, "Empty item 3 width must equal 200.dp (no shrink-wrapping)")

        // Fallback icon Add must be displayed for empty items
        composeTestRule.onNodeWithContentDescription("Home", useUnmergedTree = true).assertIsDisplayed()
        val addNodes = composeTestRule.onAllNodesWithContentDescription("Add", useUnmergedTree = true).fetchSemanticsNodes()
        assertEquals(3, addNodes.size, "3 empty items must display fallback Add icons")
    }
}
