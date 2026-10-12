package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import io.github.chandu4221.m3stage.adapter.renderer.registry.ComponentRendererRegistry
import io.github.chandu4221.m3stage.adapter.renderer.resolver.resolveModifiers
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.adapter.renderer.selection.nodeBoundsReporter
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId

private object NoIndication : androidx.compose.foundation.IndicationNodeFactory {
    override fun create(interactionSource: androidx.compose.foundation.interaction.InteractionSource): androidx.compose.ui.node.DelegatableNode {
        return object : androidx.compose.ui.Modifier.Node() {}
    }
    override fun hashCode(): Int = -1
    override fun equals(other: Any?): Boolean = other === this
}

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
        artboardOrigin: () -> Offset = { Offset.Zero }
    ) {
        val content = @Composable {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.foundation.LocalIndication provides NoIndication,
                androidx.compose.material3.LocalRippleConfiguration provides null
            ) {
                RenderInternal(node, selectedNodeId, onNodeClick, lockedNodeIds, registry, artboardOrigin)
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

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun RenderInternal(
        node: DesignNode,
        selectedNodeId: NodeId?,
        onNodeClick: (NodeId) -> Unit,
        lockedNodeIds: Set<NodeId>,
        registry: NodeBoundsRegistry?,
        artboardOrigin: () -> Offset
    ) {
        val hitTracker = remember { CanvasHitTracker() }
        var hoveredNodeId by remember { mutableStateOf<NodeId?>(null) }

        @Composable
        fun renderNode(currentNode: DesignNode, parentNode: DesignNode? = null, depth: Int = 0) {
            if (!currentNode.isVisible) return

            val isSelected = currentNode.id == selectedNodeId
            val isLocked = currentNode.id in lockedNodeIds
            val isHovered = currentNode.id == hoveredNodeId

            val baseModifier = if (registry != null) {
                Modifier.nodeBoundsReporter(
                    nodeId = currentNode.id,
                    registry = registry,
                    artboardOrigin = artboardOrigin
                )
            } else {
                Modifier
            }

            Box(modifier = baseModifier) {
                SelectionOverlay(
                    nodeId = currentNode.id,
                    depth = depth,
                    isSelected = isSelected,
                    isLocked = isLocked,
                    isHovered = isHovered,
                    hitTracker = hitTracker
                ) {
                    val renderer = ComponentRendererRegistry[currentNode.kind]
                    if (renderer != null) {
                        val elementModifier = Modifier.resolveModifiers(currentNode.modifiers, parentNode?.kind)
                        renderer.Render(currentNode, modifier = elementModifier) { child ->
                            renderNode(child, currentNode, depth + 1)
                        }
                    } else {
                        Text(text = "Unsupported: ${currentNode.kind.displayName}")
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .onGloballyPositioned { rootCoords ->
                    hitTracker.rootCoordinates = rootCoords
                }
                .onPointerEvent(PointerEventType.Move) { event ->
                    val change = event.changes.firstOrNull()
                    if (change != null) {
                        hoveredNodeId = hitTracker.findDeepestNodeAt(change.position)
                    }
                }
                .onPointerEvent(PointerEventType.Exit) {
                    hoveredNodeId = null
                }
                .pointerInput(node.id) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial).changes.firstOrNull() ?: continue
                            if (!down.pressed) continue

                            var upOrCancel = false
                            while (!upOrCancel) {
                                val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                val change = event.changes.firstOrNull()
                                if (change == null || !change.pressed) {
                                    upOrCancel = true
                                    if (change != null) {
                                        val clickedNode = hitTracker.findDeepestNodeAt(change.position)
                                        if (clickedNode != null) {
                                            onNodeClick(clickedNode)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            renderNode(node, null, 0)
        }
    }
}
