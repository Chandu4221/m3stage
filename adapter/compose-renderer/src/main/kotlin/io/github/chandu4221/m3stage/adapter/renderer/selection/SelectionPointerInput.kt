package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import io.github.chandu4221.m3stage.model.NodeId

/**
 * Transparent pointer input layer placed over the artboard to handle:
 * 1. Deepest-node hover outline detection on pointer move.
 * 2. Single-click container boundary drill-down.
 * 3. Double-click drill-down to deepest descendant under cursor.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@androidx.compose.runtime.Composable
fun Modifier.selectionPointerInput(
    registry: NodeBoundsRegistry,
    treeInfo: Map<NodeId, NodeTreeInfo>,
    screenRootId: NodeId,
    anchorState: MutableState<NodeId?>,
    onSelectNode: (NodeId?) -> Unit,
    onHoverChange: (NodeId?) -> Unit
): Modifier {
    val currentTreeInfo by androidx.compose.runtime.rememberUpdatedState(treeInfo)
    val currentAnchorState by androidx.compose.runtime.rememberUpdatedState(anchorState)
    val currentOnSelectNode by androidx.compose.runtime.rememberUpdatedState(onSelectNode)
    val currentOnHoverChange by androidx.compose.runtime.rememberUpdatedState(onHoverChange)

    return this
        .onPointerEvent(PointerEventType.Move) { event ->
            val change = event.changes.firstOrNull()
            if (change != null) {
                val hoverTarget = registry.hitTest(change.position, currentTreeInfo)
                currentOnHoverChange(hoverTarget)
            }
        }
        .onPointerEvent(PointerEventType.Exit) {
            currentOnHoverChange(null)
        }
        .pointerInput(screenRootId) {
            detectTapGestures(
                onTap = { position ->
                    val (target, newAnchor) = SelectionTargetResolver.resolveSingleClickTarget(
                        point = position,
                        anchor = currentAnchorState.value,
                        screenRootId = screenRootId,
                        treeInfo = currentTreeInfo,
                        bounds = registry.all()
                    )
                    currentAnchorState.value = newAnchor
                    currentOnSelectNode(target)
                },
                onDoubleTap = { position ->
                    val (target, newAnchor) = SelectionTargetResolver.resolveDoubleClickTarget(
                        point = position,
                        anchor = currentAnchorState.value,
                        screenRootId = screenRootId,
                        treeInfo = currentTreeInfo,
                        bounds = registry.all()
                    )
                    currentAnchorState.value = newAnchor
                    currentOnSelectNode(target)
                }
            )
        }
}
