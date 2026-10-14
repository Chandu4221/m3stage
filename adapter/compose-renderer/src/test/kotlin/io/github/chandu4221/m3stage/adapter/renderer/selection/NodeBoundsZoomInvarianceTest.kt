package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.property.PropertyValue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NodeBoundsZoomInvarianceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val idGen = object : IdGenerator {
        private var counter = 1
        override fun nextNodeId(): NodeId = NodeId("test_node_${counter++}")
        override fun nextScreenId(): ScreenId = ScreenId("screen_${counter++}")
        override fun nextProjectId(): ProjectId = ProjectId("project_${counter++}")
    }

    @Test
    fun testRegistryBoundsAreZoomInvariantAcrossScales() {
        var currentZoom by mutableStateOf(1f)
        val registry = NodeBoundsRegistry()
        var artboardOrigin = Offset.Zero

        val textNode = DesignNode(
            id = NodeId("child_text"),
            kind = ComponentKind.Text,
            props = mapOf(
                TextDefinition.TextContent.id to PropertyValue.StringValue("Zoom Invariant Text")
            )
        )
        val rootNode = DesignNode(
            id = NodeId("root_column"),
            kind = ComponentKind.Column,
            children = listOf(textNode)
        )

        composeTestRule.setContent {
            key(currentZoom) {
                Box(modifier = Modifier.size(800.dp, 800.dp)) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = currentZoom
                                scaleY = currentZoom
                                translationX = 50f
                                translationY = 30f
                            }
                            .padding(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(400.dp, 600.dp)
                                .onGloballyPositioned { coords ->
                                    if (coords.isAttached) {
                                        artboardOrigin = coords.positionInRoot()
                                    }
                                }
                        ) {
                            DesignRenderer.Render(
                                node = rootNode,
                                selectedNodeId = null,
                                onNodeClick = {},
                                registry = registry,
                                artboardOrigin = { artboardOrigin },
                                zoomProvider = { currentZoom }
                            )
                        }
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        val boundsAt100 = registry.all()
        val textRect100 = boundsAt100[textNode.id]
        val rootRect100 = boundsAt100[rootNode.id]
        assertNotNull(textRect100)
        assertNotNull(rootRect100)

        val testScales = listOf(0.5f, 0.75f, 1.25f, 2.0f)
        for (scale in testScales) {
            currentZoom = scale
            composeTestRule.waitForIdle()

            val textRectScaled = registry.get(textNode.id)
            val rootRectScaled = registry.get(rootNode.id)
            assertNotNull(textRectScaled, "Text rect missing at zoom $scale")
            assertNotNull(rootRectScaled, "Root rect missing at zoom $scale")

            assertRectClose(textRect100, textRectScaled, tolerance = 1.0f, label = "Text at zoom $scale")
            assertRectClose(rootRect100, rootRectScaled, tolerance = 1.0f, label = "Root at zoom $scale")
        }
    }

    @Test
    fun testEndToEndBottomAlignedNodeOffsetCorrectnessAtZoom() {
        var currentZoom by mutableStateOf(1f)
        val registry = NodeBoundsRegistry()
        var artboardOrigin = Offset.Zero
        var expectedArtboardHeightPx = 0f

        val navBarNode = ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
        val scaffoldNode = DesignNode(
            id = NodeId("scaffold"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(
                ComponentCatalog.ScaffoldProps.BottomBarSlot.id to listOf(navBarNode)
            )
        )

        composeTestRule.setContent {
            val density = LocalDensity.current
            expectedArtboardHeightPx = with(density) { 700.dp.toPx() }

            key(currentZoom) {
                Box(modifier = Modifier.size(800.dp, 900.dp)) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = currentZoom
                                scaleY = currentZoom
                                translationX = 100f
                                translationY = 80f
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(412.dp, 700.dp)
                                .onGloballyPositioned { coords ->
                                    if (coords.isAttached) {
                                        artboardOrigin = coords.positionInRoot()
                                    }
                                }
                        ) {
                            DesignRenderer.Render(
                                node = scaffoldNode,
                                selectedNodeId = null,
                                onNodeClick = {},
                                registry = registry,
                                artboardOrigin = { artboardOrigin },
                                zoomProvider = { currentZoom }
                            )
                        }
                    }
                }
            }
        }

        val testZooms = listOf(1.0f, 0.75f, 0.5f, 2.0f)
        for (zoom in testZooms) {
            currentZoom = zoom
            composeTestRule.waitForIdle()

            val navBarRect = registry.get(navBarNode.id)
            assertNotNull(navBarRect, "NavigationBar rect missing at zoom $zoom")

            // The NavigationBar sits at the bottom of the 700dp Scaffold.
            // Its bottom must match the artboard height within 1px tolerance.
            assertTrue(
                abs(navBarRect.bottom - expectedArtboardHeightPx) <= 1.0f,
                "NavigationBar bottom (${navBarRect.bottom}) should match artboard height ($expectedArtboardHeightPx) at zoom $zoom"
            )
        }
    }

    @Test
    fun testHitTestCorrectnessAtZoomLevels() {
        var currentZoom by mutableStateOf(1f)
        val registry = NodeBoundsRegistry()
        var artboardOrigin = Offset.Zero
        var artboardHeightPx = 0f

        val navBarNode = ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
        val scaffoldNode = DesignNode(
            id = NodeId("scaffold"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(
                ComponentCatalog.ScaffoldProps.BottomBarSlot.id to listOf(navBarNode)
            )
        )
        val treeInfo = scaffoldNode.toTreeInfo()

        composeTestRule.setContent {
            val density = LocalDensity.current
            artboardHeightPx = with(density) { 700.dp.toPx() }

            key(currentZoom) {
                Box(modifier = Modifier.size(800.dp, 900.dp)) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = currentZoom
                                scaleY = currentZoom
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(412.dp, 700.dp)
                                .onGloballyPositioned { coords ->
                                    if (coords.isAttached) {
                                        artboardOrigin = coords.positionInRoot()
                                    }
                                }
                        ) {
                            DesignRenderer.Render(
                                node = scaffoldNode,
                                selectedNodeId = null,
                                onNodeClick = {},
                                registry = registry,
                                artboardOrigin = { artboardOrigin },
                                zoomProvider = { currentZoom }
                            )
                        }
                    }
                }
            }
        }

        val testZooms = listOf(1.0f, 0.75f, 0.5f, 1.5f)
        for (zoom in testZooms) {
            currentZoom = zoom
            composeTestRule.waitForIdle()

            // Point near the bottom inside the NavigationBar
            val bottomPoint = Offset(200f, artboardHeightPx - 30f)
            val hitBottom = registry.hitTest(bottomPoint, treeInfo)
            assertNotNull(hitBottom, "Hit test should resolve a target at bottom at zoom $zoom")

            // The hit target must be the NavigationBar or one of its child NavigationBarItems
            val navBarSubtreeIds = navBarNode.toTreeInfo().keys
            assertTrue(
                hitBottom in navBarSubtreeIds,
                "Hit target $hitBottom at bottom must be inside NavigationBar subtree at zoom $zoom"
            )

            // Point near the top should NOT resolve to anything inside NavigationBar
            val topPoint = Offset(200f, 30f)
            val hitTop = registry.hitTest(topPoint, treeInfo)
            assertTrue(
                hitTop !in navBarSubtreeIds,
                "Hit target $hitTop at top must not be NavigationBar at zoom $zoom"
            )
        }
    }

    private fun assertRectClose(expected: Rect, actual: Rect, tolerance: Float, label: String) {
        assertTrue(
            abs(expected.left - actual.left) <= tolerance,
            "$label: left mismatch expected ${expected.left} but was ${actual.left}"
        )
        assertTrue(
            abs(expected.top - actual.top) <= tolerance,
            "$label: top mismatch expected ${expected.top} but was ${actual.top}"
        )
        assertTrue(
            abs(expected.right - actual.right) <= tolerance,
            "$label: right mismatch expected ${expected.right} but was ${actual.right}"
        )
        assertTrue(
            abs(expected.bottom - actual.bottom) <= tolerance,
            "$label: bottom mismatch expected ${expected.bottom} but was ${actual.bottom}"
        )
    }
}
