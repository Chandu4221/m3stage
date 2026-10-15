package io.github.chandu4221.m3stage.ui.canvas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.adapter.renderer.selection.DragReorderHelper
import io.github.chandu4221.m3stage.adapter.renderer.selection.DropTargetResult
import io.github.chandu4221.m3stage.adapter.renderer.selection.NodeBoundsRegistry
import io.github.chandu4221.m3stage.adapter.renderer.selection.SelectionChrome
import io.github.chandu4221.m3stage.adapter.renderer.selection.selectionPointerInput
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.model.effectiveWindowSizeClass
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findParent
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.device.DeviceFrame
import io.github.chandu4221.m3stage.ui.device.DeviceOrientation
import io.github.chandu4221.m3stage.ui.molecule.NodeContextMenu
import io.github.chandu4221.m3stage.ui.molecule.RenameNodeDialog

/**
 * Sealed Device Canvas ("iframe" sandbox).
 * Encapsulates the device frame bezel, Compose design renderer, local node bounds registry,
 * selection overlay, and transparent pointer input layer.
 *
 * Invariants:
 * - Local geometry and bounds are strictly internal.
 * - Zoom is used solely for stroke-width compensation inside [SelectionChrome].
 * - No outer clickable wrapping the content when in standard selection mode.
 */
@Composable
fun DeviceCanvas(
    screen: Screen,
    device: DevicePreset,
    orientation: DeviceOrientation,
    showFrame: Boolean,
    isActive: Boolean,
    selectedNodeId: NodeId?,
    lockedNodeIds: Set<NodeId>,
    onActivate: () -> Unit,
    onSelectNode: (NodeId?) -> Unit,
    selectedNodeIds: Set<NodeId> = selectedNodeId?.let { setOf(it) } ?: emptySet(),
    onToggleSelectNode: ((NodeId) -> Unit)? = null,
    onContextMenu: ((NodeId?, Offset) -> Unit)? = null,
    zoomProvider: () -> Float,
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    onCanvasDragActiveChange: ((Boolean, () -> Unit) -> Unit)? = null,
    store: EditorStore? = null,
    modifier: Modifier = Modifier
) {
    // Encapsulated Artboard-Local Selection & Bounds State
    val registry = remember(screen.id) { NodeBoundsRegistry() }
    val treeInfo = remember(screen.root) { screen.root.toTreeInfo() }
    val anchorState = remember(screen.id) { mutableStateOf<NodeId?>(null) }
    var hoveredNodeId by remember(screen.id) { mutableStateOf<NodeId?>(null) }
    var artboardOrigin by remember { mutableStateOf(Offset.Zero) }

    var activeDragNodeId by remember(screen.id) { mutableStateOf<NodeId?>(null) }
    var activeDropTarget by remember(screen.id) { mutableStateOf<DropTargetResult?>(null) }

    var contextMenuOffset by remember(screen.id) { mutableStateOf<Offset?>(null) }
    var contextMenuTargetId by remember(screen.id) { mutableStateOf<NodeId?>(null) }
    var nodeToRename by remember(screen.id) { mutableStateOf<DesignNode?>(null) }

    val selectedNode = remember(selectedNodeId, screen.root) {
        selectedNodeId?.let { screen.root.findNode(it) }
    }
    val dropTargetColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .then(
                if (draggedComponentKind != null) {
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onDropOnScreen(screen.id, draggedComponentKind) }
                        )
                        .drawBehind {
                            val zoom = zoomProvider().coerceAtLeast(0.01f)
                            val strokeWidth = (3.dp.toPx() / zoom).coerceAtLeast(0.5f)
                            val dashLength = 12f / zoom
                            drawRoundRect(
                                color = dropTargetColor,
                                size = size,
                                cornerRadius = CornerRadius(24.dp.toPx()),
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, dashLength), 0f)
                                )
                            )
                        }
                } else {
                    Modifier
                }
            )
    ) {
        DeviceFrame(
            preset = device,
            orientation = orientation,
            showFrame = showFrame
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coords ->
                        if (coords.isAttached) {
                            artboardOrigin = coords.positionInRoot()
                        }
                    }
            ) {
                // 1. Rendered components (below pointer input layer)
                val widthDp = screen.resolveWidthDp(projectDefault = device)
                val windowSizeClass = screen.effectiveWindowSizeClass(widthDp)

                DesignRenderer.Render(
                    node = screen.root,
                    selectedNodeId = selectedNodeId,
                    onNodeClick = onSelectNode,
                    lockedNodeIds = lockedNodeIds,
                    registry = registry,
                    artboardOrigin = { artboardOrigin },
                    zoomProvider = zoomProvider,
                    windowSizeClass = windowSizeClass,
                    isAdaptive = screen.adaptive
                )

                // 2. Figma-style selection & hover chrome
                SelectionChrome(
                    registry = registry,
                    hoveredNodeId = hoveredNodeId,
                    selectedNodeId = selectedNodeId,
                    selectedNode = selectedNode,
                    selectedNodeIds = selectedNodeIds,
                    draggedNodeId = activeDragNodeId,
                    dropTarget = activeDropTarget,
                    zoomProvider = zoomProvider,
                    modifier = Modifier.fillMaxSize()
                )

                // 3. Top transparent pointer overlay (intercepts gestures before child buttons/widgets)
                if (draggedComponentKind != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onDropOnScreen(screen.id, draggedComponentKind) }
                            )
                    )
                } else {
                    val hoverCursor = if (hoveredNodeId != null) {
                        ToolCursorMapper.resolveCursor(CanvasPointerTool.Select, CanvasCursorOverride.ClickableHover)
                    } else {
                        ToolCursorMapper.resolveCursor(CanvasPointerTool.Select)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerHoverIcon(hoverCursor)
                            .selectionPointerInput(
                                registry = registry,
                                treeInfo = treeInfo,
                                screenRootId = screen.root.id,
                                anchorState = anchorState,
                                selectedNodeId = selectedNodeId,
                                onSelectNode = { target ->
                                    onActivate()
                                    onSelectNode(target)
                                },
                                onHoverChange = { hoveredNodeId = it },
                                onToggleSelectNode = { target ->
                                    onActivate()
                                    onToggleSelectNode?.invoke(target)
                                },
                                onContextMenu = { target, offset ->
                                    onActivate()
                                    if (target != null && target != screen.root.id) {
                                        if (target !in selectedNodeIds) {
                                            onSelectNode(target)
                                        }
                                        contextMenuTargetId = target
                                    } else {
                                        contextMenuTargetId = null
                                    }
                                    contextMenuOffset = offset
                                    onContextMenu?.invoke(target, offset)
                                },
                                onDragStart = { targetNodeId, pointerPos ->
                                    onActivate()
                                    if (targetNodeId != selectedNodeId) {
                                        onSelectNode(targetNodeId)
                                    }
                                    activeDragNodeId = targetNodeId
                                    onCanvasDragActiveChange?.invoke(true) {
                                        activeDragNodeId = null
                                        activeDropTarget = null
                                    }
                                    val parent = screen.root.findParent(targetNodeId)
                                    if (parent != null) {
                                        val slotId = parent.findSlotForNode(targetNodeId)
                                        val children = if (slotId != null) parent.slots[slotId] ?: emptyList() else parent.children
                                        val isVertical = parent.kind != ComponentKind.Row
                                        val parentBounds = registry[parent.id]
                                        val siblings = children.mapNotNull { child -> registry[child.id]?.let { child.id to it } }
                                        activeDropTarget = DragReorderHelper.computeDropTarget(pointerPos, targetNodeId, parentBounds, siblings, isVertical)
                                    }
                                },
                                onDragMove = { pointerPos ->
                                    val currentDragId = activeDragNodeId
                                    if (currentDragId != null) {
                                        val parent = screen.root.findParent(currentDragId)
                                        if (parent != null) {
                                            val slotId = parent.findSlotForNode(currentDragId)
                                            val children = if (slotId != null) parent.slots[slotId] ?: emptyList() else parent.children
                                            val isVertical = parent.kind != ComponentKind.Row
                                            val parentBounds = registry[parent.id]
                                            val siblings = children.mapNotNull { child -> registry[child.id]?.let { child.id to it } }
                                            activeDropTarget = DragReorderHelper.computeDropTarget(pointerPos, currentDragId, parentBounds, siblings, isVertical)
                                        } else {
                                            activeDropTarget = null
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val currentDragId = activeDragNodeId
                                    val currentTarget = activeDropTarget
                                    if (currentDragId != null && currentTarget != null && currentTarget.isValid) {
                                        store?.reorderNode(currentDragId, currentTarget.targetIndex)
                                    }
                                    activeDragNodeId = null
                                    activeDropTarget = null
                                    onCanvasDragActiveChange?.invoke(false) {}
                                },
                                onDragCancel = {
                                    activeDragNodeId = null
                                    activeDropTarget = null
                                    onCanvasDragActiveChange?.invoke(false) {}
                                }
                            )
                    )
                }

                // 4. Context Menu
                if (contextMenuOffset != null) {
                    val density = androidx.compose.ui.platform.LocalDensity.current
                    val dpOffset = with(density) {
                        androidx.compose.ui.unit.DpOffset(contextMenuOffset!!.x.toDp(), contextMenuOffset!!.y.toDp())
                    }
                    val targetNode = contextMenuTargetId?.let { screen.root.findNode(it) }
                    val isLocked = contextMenuTargetId?.let { lockedNodeIds.contains(it) } ?: false
                    val canPaste = store?.clipboardDelegate?.clip?.value != null
                    val hasMultipleSelection = selectedNodeIds.size > 1 && contextMenuTargetId in selectedNodeIds

                    NodeContextMenu(
                        expanded = true,
                        offset = dpOffset,
                        targetNode = targetNode,
                        isLocked = isLocked,
                        canPaste = canPaste,
                        hasMultipleSelection = hasMultipleSelection,
                        onDismissRequest = { contextMenuOffset = null },
                        onRename = {
                            contextMenuOffset = null
                            nodeToRename = targetNode
                        },
                        onCopy = {
                            contextMenuOffset = null
                            targetNode?.let { store?.copyNode(it.id) }
                        },
                        onCut = {
                            contextMenuOffset = null
                            targetNode?.let { store?.cutNode(it.id) }
                        },
                        onPaste = {
                            contextMenuOffset = null
                            store?.pasteNode()
                        },
                        onDuplicate = {
                            contextMenuOffset = null
                            if (hasMultipleSelection) {
                                store?.duplicateSelectedNodes()
                            } else {
                                targetNode?.let { store?.duplicateNode(it.id) }
                            }
                        },
                        onDelete = {
                            contextMenuOffset = null
                            if (hasMultipleSelection) {
                                store?.removeSelectedNodes()
                            } else {
                                targetNode?.let { store?.removeNode(it.id) }
                            }
                        },
                        onToggleLock = {
                            contextMenuOffset = null
                            targetNode?.let { store?.toggleLock(it.id) }
                        },
                        onToggleVisibility = {
                            contextMenuOffset = null
                            targetNode?.let { store?.toggleVisibility(it.id) }
                        },
                        onSelectAll = {
                            contextMenuOffset = null
                            store?.selectAllNodes(screen)
                        },
                        onDeselect = {
                            contextMenuOffset = null
                            store?.selectNode(null)
                        }
                    )
                }

                val targetToRename = nodeToRename
                if (targetToRename != null) {
                    RenameNodeDialog(
                        initialLabel = targetToRename.label,
                        defaultName = targetToRename.kind.displayName,
                        onDismiss = { nodeToRename = null },
                        onConfirm = { newLabel ->
                            store?.renameNode(targetToRename.id, newLabel)
                            nodeToRename = null
                        }
                    )
                }
            }
        }
    }
}
