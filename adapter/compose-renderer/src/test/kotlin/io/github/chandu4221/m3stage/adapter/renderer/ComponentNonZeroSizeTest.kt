package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.IdGenerator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(Parameterized::class)
class ComponentNonZeroSizeTest(
    private val definition: ComponentDefinition
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<ComponentDefinition> = ComponentCatalog.all
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val idGen = object : IdGenerator {
        private var counter = 1
        override fun nextNodeId(): NodeId = NodeId("node_${counter++}")
        override fun nextScreenId(): ScreenId = ScreenId("screen_${counter++}")
        override fun nextProjectId(): ProjectId = ProjectId("project_${counter++}")
    }

    @Test
    fun testDefaultSubtreeRendersWithNonZeroDimensions() {
        val rootNode = definition.createDefaultSubtree(idGen)
        val registry = NodeBoundsRegistry()

        composeTestRule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Box(modifier = Modifier.size(412.dp, 915.dp)) {
                    DesignRenderer.Render(
                        node = rootNode,
                        selectedNodeId = null,
                        onNodeClick = {},
                        registry = registry
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        fun assertNodeAndDescendantsNonZero(node: io.github.chandu4221.m3stage.model.DesignNode) {
            val b = registry.get(node.id)
            assertNotNull(b, "Node ${node.kind} (${node.id.value}) was not reported in NodeBoundsRegistry")
            assertTrue(
                b.width > 0f,
                "Node ${node.kind} (${node.id.value}) in subtree of ${definition.kind} must render with width > 0, but was ${b.width}"
            )
            assertTrue(
                b.height > 0f,
                "Node ${node.kind} (${node.id.value}) in subtree of ${definition.kind} must render with height > 0, but was ${b.height}"
            )
            node.allDirectChildren.forEach { assertNodeAndDescendantsNonZero(it) }
        }

        assertNodeAndDescendantsNonZero(rootNode)
    }
}
