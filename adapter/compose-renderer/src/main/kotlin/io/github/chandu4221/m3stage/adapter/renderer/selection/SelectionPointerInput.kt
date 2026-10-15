package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.NodeId

/**
 * Transparent pointer input layer placed over the artboard to handle:
 * 1. Deepest-node hover outline detection on pointer move.
 * 2. Single-click container boundary drill-down.
 * 3. Double-click drill-down to deepest descendant under cursor.
 * 4. Shift+Click multi-select toggle.
 * 5. Right-click context menu invocation.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@androidx.compose.runtime.Composable
fun Modifier.selectionPointerInput(
    registry: NodeBoundsRegistry,
    treeInfo: Map<NodeId, NodeTreeInfo>,
    screenRootId: NodeId,
    anchorState: MutableState<NodeId?>,
    onSelectNode: (NodeId?) -> Unit,
    onHoverChange: (NodeId?) -> Unit,
    selectedNodeId: NodeId? = null,
    onToggleSelectNode: ((NodeId) -> Unit)? = null,
    onContextMenu: ((NodeId?, Offset) -> Unit)? = null,
    onDragStart: ((NodeId, Offset) -> Unit)? = null,
    onDragMove: ((Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null
): Modifier {
    val currentTreeInfo by androidx.compose.runtime.rememberUpdatedState(treeInfo)
    val currentAnchorState by androidx.compose.runtime.rememberUpdatedState(anchorState)
    val currentOnSelectNode by androidx.compose.runtime.rememberUpdatedState(onSelectNode)
    val currentOnHoverChange by androidx.compose.runtime.rememberUpdatedState(onHoverChange)
    val currentSelectedNodeId by androidx.compose.runtime.rememberUpdatedState(selectedNodeId)
    val currentOnToggleSelectNode by androidx.compose.runtime.rememberUpdatedState(onToggleSelectNode)
    val currentOnContextMenu by androidx.compose.runtime.rememberUpdatedState(onContextMenu)
    val currentOnDragStart by androidx.compose.runtime.rememberUpdatedState(onDragStart)
    val currentOnDragMove by androidx.compose.runtime.rememberUpdatedState(onDragMove)
    val currentOnDragEnd by androidx.compose.runtime.rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by androidx.compose.runtime.rememberUpdatedState(onDragCancel)

    var isShiftHeld by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    return this
        .onPointerEvent(PointerEventType.Press) { event ->
            isShiftHeld = event.keyboardModifiers.isShiftPressed
            if (event.buttons.isSecondaryPressed || event.button == PointerButton.Secondary) {
                val change = event.changes.firstOrNull()
                if (change != null) {
                    change.consume()
                    val target = registry.hitTest(change.position, currentTreeInfo)
                    currentOnContextMenu?.invoke(target, change.position)
                }
            }
        }
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
            val dragThresholdPx = 4.dp.toPx()
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.type == PointerType.Mouse && currentEvent.buttons.isSecondaryPressed) {
                    return@awaitEachGesture
                }
                val currentlySelected = currentSelectedNodeId
                val targetNode = if (currentlySelected != null && currentlySelected != screenRootId && registry[currentlySelected]?.contains(down.position) == true) {
                    currentlySelected
                } else {
                    registry.hitTest(down.position, currentTreeInfo)
                }

                if (targetNode == null || targetNode == screenRootId) {
                    return@awaitEachGesture
                }

                val startPos = down.position
                var isDragging = false

                try {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            if (isDragging) {
                                currentOnDragEnd?.invoke()
                            }
                            break
                        }

                        val distance = (change.position - startPos).getDistance()
                        if (!isDragging) {
                            if (distance > dragThresholdPx) {
                                isDragging = true
                                change.consume()
                                currentOnDragStart?.invoke(targetNode, change.position)
                            }
                        } else {
                            change.consume()
                            currentOnDragMove?.invoke(change.position)
                        }
                    }
                } catch (e: Exception) {
                    if (isDragging) {
                        currentOnDragCancel?.invoke()
                    }
                }
            }
        }
        .pointerInput(screenRootId) {
            detectTapGestures(
                onTap = { position ->
                    val shiftAtTap = isShiftHeld
                    isShiftHeld = false
                    val (target, newAnchor) = SelectionTargetResolver.resolveSingleClickTarget(
                        point = position,
                        anchor = currentAnchorState.value,
                        screenRootId = screenRootId,
                        treeInfo = currentTreeInfo,
                        bounds = registry.all()
                    )
                    if (shiftAtTap && target != null && currentOnToggleSelectNode != null) {
                        currentOnToggleSelectNode?.invoke(target)
                    } else {
                        currentAnchorState.value = newAnchor
                        currentOnSelectNode(target)
                    }
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
