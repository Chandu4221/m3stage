package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import io.github.chandu4221.m3stage.adapter.renderer.registry.ComponentRendererRegistry
import io.github.chandu4221.m3stage.adapter.renderer.resolver.resolveModifiers
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.adapter.renderer.selection.nodeBoundsReporter
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

private object NoIndication : androidx.compose.foundation.IndicationNodeFactory {
    override fun create(interactionSource: androidx.compose.foundation.interaction.InteractionSource): androidx.compose.ui.node.DelegatableNode {
        return object : androidx.compose.ui.Modifier.Node() {}
    }
    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

val LocalWindowSizeClass = androidx.compose.runtime.compositionLocalOf { io.github.chandu4221.m3stage.model.WindowSizeClass.Compact }
val LocalIsAdaptiveScreen = androidx.compose.runtime.compositionLocalOf { false }

object DesignRenderer {

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    fun Render(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId> = emptySet(),
        scheme: ColorScheme? = null,
        registry: NodeBoundsRegistry? = null,
        artboardOrigin: () -> Offset = { Offset.Zero },
        zoomProvider: () -> Float = { 1f },
        windowSizeClass: io.github.chandu4221.m3stage.model.WindowSizeClass = io.github.chandu4221.m3stage.model.WindowSizeClass.Compact,
        isAdaptive: Boolean = false
    ) {
        val content = @Composable {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.foundation.LocalIndication provides NoIndication,
                androidx.compose.material3.LocalRippleConfiguration provides null,
                LocalWindowSizeClass provides windowSizeClass,
                LocalIsAdaptiveScreen provides isAdaptive
            ) {
                RenderInternal(node, registry, artboardOrigin, zoomProvider)
            }
        }

        if (scheme != null) {
            MaterialTheme(colorScheme = scheme) {
                content()
            }
        } else {
            content()
        }
    }


    @Composable
    private fun RenderInternal(
        node: DesignNode,
        registry: NodeBoundsRegistry?,
        artboardOrigin: () -> Offset,
        zoomProvider: () -> Float = { 1f }
    ) {
        @Composable
        fun renderNode(
            currentNode: DesignNode,
            parentNode: DesignNode? = null,
            ancestors: List<ComponentKind> = emptyList(),
            depth: Int = 0
        ) {
            if (!currentNode.isVisible) return

            val baseModifier = if (registry != null) {
                Modifier.nodeBoundsReporter(
                    nodeId = currentNode.id,
                    registry = registry,
                    artboardOrigin = artboardOrigin,
                    zoomProvider = zoomProvider
                )
            } else {
                Modifier
            }

            val renderer = ComponentRendererRegistry[currentNode.kind]
            val rowScope = io.github.chandu4221.m3stage.adapter.renderer.component.navigation.LocalRowScope.current
            val isRail = io.github.chandu4221.m3stage.adapter.renderer.component.navigation.LocalNavigationRailActive.current

            val resolvedModifier = Modifier.resolveModifiers(currentNode.modifiers, parentNode?.kind)
            val elementModifier = if (shouldRenderAsPlaceholder(currentNode, ancestors)) {
                resolvedModifier.alpha(0.55f)
            } else {
                resolvedModifier
            }

            val currentAncestors = ancestors + currentNode.kind

            if ((rowScope != null || isRail) && currentNode.kind == io.github.chandu4221.m3stage.component.ComponentKind.NavigationBarItem) {
                if (renderer != null) {
                    renderer.Render(currentNode, modifier = baseModifier.then(elementModifier)) { child ->
                        renderNode(child, currentNode, currentAncestors, depth + 1)
                    }
                } else {
                    Text(text = "Unsupported: ${currentNode.kind.displayName}")
                }
            } else {
                Box(modifier = baseModifier) {
                    if (renderer != null) {
                        renderer.Render(currentNode, modifier = elementModifier) { child ->
                            renderNode(child, currentNode, currentAncestors, depth + 1)
                        }
                    } else {
                        Text(text = "Unsupported: ${currentNode.kind.displayName}")
                    }
                }
            }
        }

        renderNode(node, null, emptyList(), 0)
    }
}

/**
 * Set of component kinds that represent interactive widgets or their specialized containers.
 * Neither interactive components nor their authored content receive placeholder alpha.
 */
val INTERACTIVE_KINDS: Set<ComponentKind> = setOf(
    ComponentKind.Button,
    ComponentKind.IconButton,
    ComponentKind.FloatingActionButton,
    ComponentKind.OutlinedButton,
    ComponentKind.TextButton,
    ComponentKind.TextField,
    ComponentKind.OutlinedTextField,
    ComponentKind.TopAppBar,
    ComponentKind.NavigationBar,
    ComponentKind.NavigationBarItem,
    ComponentKind.Switch,
    ComponentKind.Checkbox,
    ComponentKind.RadioButton,
    ComponentKind.Slider
)

/**
 * Pure predicate determining whether [node] should be rendered with placeholder alpha (0.55f).
 *
 * Rules:
 * 1. [node.isPrefilled] must be true.
 * 2. [node.kind] must be [ComponentKind.Text] or [ComponentKind.Icon] (non-interactive leaf).
 * 3. [ancestors] must contain NO interactive component kinds.
 */
fun shouldRenderAsPlaceholder(node: DesignNode, ancestors: List<ComponentKind>): Boolean {
    if (!node.isPrefilled) return false
    if (node.kind != ComponentKind.Text && node.kind != ComponentKind.Icon) return false
    return ancestors.none { it in INTERACTIVE_KINDS }
}
