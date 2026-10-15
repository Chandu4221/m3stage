package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.NavigationBarItemDefinition
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.WindowSizeClass
import io.github.chandu4221.m3stage.property.PropertyValue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AdaptiveNavigationBarRendererTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAdaptiveScreenInExpandedSizeClassRendersNavigationRail() {
        val navBar = DesignNode(
            id = NodeId("navbar_test"),
            kind = ComponentKind.NavigationBar,
            children = emptyList()
        )

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(1000.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = navBar,
                        selectedNodeId = null,
                        onNodeClick = {},
                        windowSizeClass = WindowSizeClass.Expanded,
                        isAdaptive = true
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("+ Empty NavigationRail — Add Composables").assertIsDisplayed()
    }

    @Test
    fun testAdaptiveScreenInCompactSizeClassRendersNavigationBar() {
        val navBar = DesignNode(
            id = NodeId("navbar_test"),
            kind = ComponentKind.NavigationBar,
            children = emptyList()
        )

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(400.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = navBar,
                        selectedNodeId = null,
                        onNodeClick = {},
                        windowSizeClass = WindowSizeClass.Compact,
                        isAdaptive = true
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("+ Empty NavigationBar — Add Composables").assertIsDisplayed()
    }

    @Test
    fun testFixedScreenInExpandedSizeClassRendersNavigationBarAlways() {
        val navBar = DesignNode(
            id = NodeId("navbar_test"),
            kind = ComponentKind.NavigationBar,
            children = emptyList()
        )

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(1000.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = navBar,
                        selectedNodeId = null,
                        onNodeClick = {},
                        windowSizeClass = WindowSizeClass.Expanded,
                        isAdaptive = false // Fixed screen
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("+ Empty NavigationBar — Add Composables").assertIsDisplayed()
    }

    @Test
    fun testAdaptiveScreenWithItemsRendersRailGeometryInExpanded() {
        val labelText = DesignNode(
            id = NodeId("item_label"),
            kind = ComponentKind.Text,
            props = mapOf(TextDefinition.TextContent.id to PropertyValue.StringValue("Home"))
        )
        val navItem = DesignNode(
            id = NodeId("item_1"),
            kind = ComponentKind.NavigationBarItem,
            props = mapOf(NavigationBarItemDefinition.Selected.id to PropertyValue.BooleanValue(true)),
            slots = mapOf(NavigationBarItemDefinition.LabelSlot.id to listOf(labelText))
        )
        val navBar = DesignNode(
            id = NodeId("navbar_with_items"),
            kind = ComponentKind.NavigationBar,
            children = listOf(navItem)
        )

        val registry = NodeBoundsRegistry()

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(1000.dp, 800.dp)) {
                    DesignRenderer.Render(
                        node = navBar,
                        selectedNodeId = null,
                        onNodeClick = {},
                        registry = registry,
                        windowSizeClass = WindowSizeClass.Expanded,
                        isAdaptive = true
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()

        val navBounds = registry.get(navBar.id)
        assertNotNull(navBounds, "NavBar bounds must be registered")
        // NavigationRail has fixed width (80dp in standard M3, well below 300px), and fills vertical height (800dp)
        assertTrue(navBounds.width < 300f, "NavigationRail width should be rail width (~80dp), but was ${navBounds.width}")
        assertTrue(navBounds.height > 500f, "NavigationRail height should fill vertical height (800dp), but was ${navBounds.height}")
    }
}
