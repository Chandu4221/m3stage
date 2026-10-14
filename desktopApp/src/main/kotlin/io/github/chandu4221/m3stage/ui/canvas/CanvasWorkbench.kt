package io.github.chandu4221.m3stage.ui.canvas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpOffset

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportController
import io.github.chandu4221.m3stage.ui.device.DeviceOrientation


/**
 * Parent Canvas (CanvasWorkbench): Infinite studio desk.
 *
 * Responsibilities:
 * - Dotted grid background (fixed 24dp screen-space dots).
 * - Viewport camera transforms via draw-phase [graphicsLayer].
 * - Middle-mouse always-pan and Hand-tool drags.
 * - Ctrl+scroll cursor-anchored zoom and plain scroll pan.
 * - Multi-screen Row arrangement and screen toolbar counter-scaling (1 / zoom).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasWorkbench(
    screens: List<Screen>,
    activeScreenId: ScreenId?,
    onSelectScreen: (ScreenId?) -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId?) -> Unit,
    lockedNodeIds: Set<NodeId>,
    viewportController: CanvasViewportController,
    projectDefaultDevice: DevicePreset = DevicePreset.Default,
    canMoveLeft: (Int) -> Boolean = { it > 0 },
    canMoveRight: (Int) -> Boolean = { it < screens.lastIndex },
    onMoveLeft: (Int) -> Unit = {},
    onMoveRight: (Int) -> Unit = {},
    onRenameScreen: (ScreenId, String) -> Unit = { _, _ -> },
    onDeleteScreen: (ScreenId) -> Unit = {},
    onDuplicateScreen: (ScreenId) -> Unit = {},
    onMoveScreen: (ScreenId, Float, Float) -> Unit = { _, _, _ -> },
    onDeviceChange: (ScreenId, DevicePreset) -> Unit = { _, _ -> },
    onOrientationToggle: (ScreenId) -> Unit = {},
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    val density = LocalDensity.current
    val viewportState = viewportController.viewportState
    val activeTool = viewportState.value.activeTool
    var isDraggingCanvas by remember { mutableStateOf(false) }

    val cursorOverride = if (isDraggingCanvas) CanvasCursorOverride.Panning else CanvasCursorOverride.None
    val effectiveCursor = ToolCursorMapper.resolveCursor(activeTool, cursorOverride)
    val isPanningMode = activeTool == CanvasPointerTool.Pan || isDraggingCanvas

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size ->
                viewportController.setViewportSize(Size(size.width.toFloat(), size.height.toFloat()))
            }
            .pointerHoverIcon(effectiveCursor, overrideDescendants = isPanningMode)
            .background(MaterialTheme.colorScheme.surfaceDim)
            // P1: Dotted grid in screen-space (does NOT scale with zoom)
            .drawBehind {
                val dotSpacing = 24.dp.toPx()
                val dotRadius = 1.dp.toPx()
                val cols = (size.width / dotSpacing).toInt() + 1
                val rows = (size.height / dotSpacing).toInt() + 1
                for (i in 0..cols) {
                    for (j in 0..rows) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(i * dotSpacing, j * dotSpacing)
                        )
                    }
                }
            }
            // P2: Pan Gestures (Hand tool OR Middle-mouse always-pan)
            .pointerInput(viewportState.value.activeTool) {
                detectDragGestures(
                    onDragStart = {
                        if (viewportState.value.activeTool == CanvasPointerTool.Pan) {
                            isDraggingCanvas = true
                        }
                    },
                    onDragEnd = { isDraggingCanvas = false },
                    onDragCancel = { isDraggingCanvas = false },
                    onDrag = { change, dragAmount ->
                        if (viewportState.value.activeTool == CanvasPointerTool.Pan) {
                            change.consume()
                            viewportController.panBy(dragAmount)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onSelectScreen(null)
                        onNodeClick(null)
                    }
                )
            }
            // P2 (Middle-mouse pan) & P3/P4: Mouse Wheel Scroll
            .onPointerEvent(PointerEventType.Move) { event ->
                if (event.buttons.isTertiaryPressed) {
                    isDraggingCanvas = true
                    val change = event.changes.firstOrNull() ?: return@onPointerEvent
                    val delta = change.position - change.previousPosition
                    if (delta != Offset.Zero) {
                        change.consume()
                        viewportController.panBy(delta)
                    }
                } else if (isDraggingCanvas && activeTool != CanvasPointerTool.Pan) {
                    isDraggingCanvas = false
                }
            }
            .onPointerEvent(PointerEventType.Release) { event ->
                if (!event.buttons.isTertiaryPressed && activeTool != CanvasPointerTool.Pan) {
                    isDraggingCanvas = false
                }
            }
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val change = event.changes.firstOrNull() ?: return@onPointerEvent
                val deltaY = change.scrollDelta.y
                val isCtrl = event.keyboardModifiers.isCtrlPressed

                if (isCtrl) {
                    // P4: Cursor-anchored zoom
                    val zoomFactor = if (deltaY < 0) 1.1f else 0.9f
                    viewportController.zoomAt(change.position, zoomFactor)
                } else {
                    // Plain scroll pan
                    val deltaX = change.scrollDelta.x
                    viewportController.panBy(Offset(-deltaX * 20f, -deltaY * 20f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // --- TRANSFORMED ARTBOARDS CONTAINER (P6: Draw-phase reads only) ---
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .graphicsLayer {
                    val current = viewportState.value
                    translationX = current.panOffset.x
                    translationY = current.panOffset.y
                    scaleX = current.zoom
                    scaleY = current.zoom
                }
                .padding(vertical = 56.dp, horizontal = 72.dp)
        ) {
            Box(
                modifier = Modifier.wrapContentSize(unbounded = true)
            ) {
                screens.forEachIndexed { index, screen ->
                    key(screen.id) {
                        val isActive = screen.id == activeScreenId
                        val activeDevice = screen.resolveDevice(projectDefaultDevice)
                        val orientation = screen.orientation
                        var showFrame by remember(screen.id) { mutableStateOf(true) }

                        val currentW = if (orientation == DeviceOrientation.Portrait) activeDevice.widthDp else activeDevice.heightDp
                        val currentH = if (orientation == DeviceOrientation.Portrait) activeDevice.heightDp else activeDevice.widthDp

                        // Visual-only drag delta: written by FloatingArtboardToolbar via onDragDelta.
                        // Scoped exclusively to active gesture lifetime; reset to Zero between gestures.
                        var dragDeltaDp by remember(screen.id) { mutableStateOf(Offset.Zero) }
                        var dragStartCanvasPos by remember(screen.id) { mutableStateOf(Offset.Zero) }

                        // Rendered position: domain position + live drag delta from gesture start
                        val screenX = screen.canvasX + dragDeltaDp.x
                        val screenY = screen.canvasY + dragDeltaDp.y

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = screenX.dp.roundToPx(),
                                        y = screenY.dp.roundToPx()
                                    )
                                }
                        ) {
                            // P5 / P3-18: Floating Artboard Toolbar (Counter-scaled at 1 / zoom)
                            // Anchored at TransformOrigin(0.5f, 1f) to keep bottom edge exactly 12dp above artboard across all zoom levels
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    val z = viewportState.value.zoom
                                    scaleX = 1f / z
                                    scaleY = 1f / z
                                    transformOrigin = TransformOrigin(0.5f, 1f)
                                }
                            ) {
                                FloatingArtboardToolbar(
                                    screen = screen,
                                    isActive = isActive,
                                    activeDevice = activeDevice,
                                    orientation = orientation,
                                    showFrame = showFrame,
                                    widthDp = currentW,
                                    heightDp = currentH,
                                    canvasZoom = viewportState.value.zoom,
                                    onActivate = { onSelectScreen(screen.id) },
                                    onDeviceChange = { onDeviceChange(screen.id, it) },
                                    onOrientationToggle = { onOrientationToggle(screen.id) },
                                    onFrameToggle = { showFrame = it },
                                    canMoveLeft = canMoveLeft(index),
                                    canMoveRight = canMoveRight(index),
                                    canDeleteScreen = screens.size > 1,
                                    onMoveLeft = { onMoveLeft(index) },
                                    onMoveRight = { onMoveRight(index) },
                                    onRename = { onRenameScreen(screen.id, it) },
                                    onDuplicate = { onDuplicateScreen(screen.id) },
                                    onDelete = { onDeleteScreen(screen.id) },
                                    onDragStart = {
                                        dragStartCanvasPos = Offset(screen.canvasX, screen.canvasY)
                                        dragDeltaDp = Offset.Zero
                                    },
                                    onDragDelta = { totalDeltaDp ->
                                        dragDeltaDp = totalDeltaDp
                                    },
                                    onDragEnd = { finalDeltaDp ->
                                        val startPos = dragStartCanvasPos
                                        dragDeltaDp = Offset.Zero
                                        if (finalDeltaDp != Offset.Zero) {
                                            onMoveScreen(screen.id, startPos.x + finalDeltaDp.x, startPos.y + finalDeltaDp.y)
                                        }
                                    },
                                    onDragCancel = {
                                        dragDeltaDp = Offset.Zero
                                    }
                                )
                            }

                            // Sealed Device Canvas (Content inside scaled container)
                            DeviceCanvas(
                                screen = screen,
                                device = activeDevice,
                                orientation = orientation,
                                showFrame = showFrame,
                                isActive = isActive,
                                selectedNodeId = if (isActive) selectedNodeId else null,
                                lockedNodeIds = lockedNodeIds,
                                onActivate = { onSelectScreen(screen.id) },
                                onSelectNode = { nodeId ->
                                    onSelectScreen(screen.id)
                                    onNodeClick(nodeId)
                                },
                                zoomProvider = { viewportState.value.zoom },
                                draggedComponentKind = draggedComponentKind,
                                onDropOnScreen = onDropOnScreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun FloatingArtboardToolbar(
    screen: Screen,
    isActive: Boolean,
    activeDevice: DevicePreset,
    orientation: DeviceOrientation,
    showFrame: Boolean,
    widthDp: Int,
    heightDp: Int,
    canvasZoom: Float,
    onActivate: () -> Unit,
    onDeviceChange: (DevicePreset) -> Unit,
    onOrientationToggle: () -> Unit,
    onFrameToggle: (Boolean) -> Unit,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    canDeleteScreen: Boolean = true,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRename: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit = {},
    onDragDelta: (totalDeltaDp: Offset) -> Unit = {},
    onDragEnd: (finalDeltaDp: Offset) -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    val currentOnActivate by rememberUpdatedState(onActivate)
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragDelta by rememberUpdatedState(onDragDelta)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val currentZoom by rememberUpdatedState(canvasZoom)
    val currentDensity by rememberUpdatedState(LocalDensity.current)

    var deviceDropdownExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = if (isActive) 4.dp else 2.dp,
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .clickable(onClick = onActivate)
            .pointerInput(screen.id) {
                var sessionDeltaDp = Offset.Zero
                var startZoom = 1f
                var densityVal = 1f

                detectDragGestures(
                    onDragStart = {
                        currentOnActivate()
                        sessionDeltaDp = Offset.Zero
                        startZoom = currentZoom
                        densityVal = currentDensity.density
                        currentOnDragStart()
                    },
                    onDragEnd = {
                        val finalDelta = sessionDeltaDp
                        sessionDeltaDp = Offset.Zero
                        currentOnDragEnd(finalDelta)
                    },
                    onDragCancel = {
                        sessionDeltaDp = Offset.Zero
                        currentOnDragCancel()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        sessionDeltaDp += Offset(
                            dragAmount.x / (densityVal * startZoom),
                            dragAmount.y / (densityVal * startZoom)
                        )
                        currentOnDragDelta(sessionDeltaDp)
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Drag grip indicator & Screen Name Badge
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Move Screen",
                modifier = Modifier.size(16.dp),
                tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = screen.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            VerticalDivider(modifier = Modifier.height(18.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Device Preset Dropdown
            Box {
                FilledTonalButton(
                    onClick = {
                        onActivate()
                        deviceDropdownExpanded = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Device",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(activeDevice.displayName, style = MaterialTheme.typography.labelMedium)
                }

                DropdownMenu(
                    expanded = deviceDropdownExpanded,
                    onDismissRequest = { deviceDropdownExpanded = false }
                ) {
                    DevicePreset.entries.forEach { preset ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(preset.displayName, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${preset.widthDp} × ${preset.heightDp} dp",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onDeviceChange(preset)
                                deviceDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Orientation Toggle
            ToolbarActionButton(
                icon = if (orientation == DeviceOrientation.Portrait)
                    Icons.Default.CropPortrait else Icons.Default.CropLandscape,
                contentDescription = "Toggle Orientation",
                tooltip = "Toggle Orientation",
                onClick = {
                    onActivate()
                    onOrientationToggle()
                }
            )

            // Hardware Bezel Toggle
            TooltipArea(
                delayMillis = 500,
                tooltip = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "Toggle Hardware Frame",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                tooltipPlacement = TooltipPlacement.ComponentRect(
                    anchor = Alignment.TopCenter,
                    alignment = Alignment.TopCenter,
                    offset = DpOffset(x = 0.dp, y = (-8).dp)
                )
            ) {
                FilledIconToggleButton(
                    checked = showFrame,
                    onCheckedChange = {
                        onActivate()
                        onFrameToggle(it)
                    },
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Frame", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Dimension Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "$widthDp × $heightDp dp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Separator 1: Identity | Reorder
            VerticalDivider(modifier = Modifier.height(18.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Reorder Group: Move Left | Move Right
            ToolbarActionButton(
                icon = Icons.Default.ChevronLeft,
                contentDescription = "Move Left",
                tooltip = if (canMoveLeft) "Move Left" else null,
                enabled = canMoveLeft,
                onClick = {
                    onActivate()
                    onMoveLeft()
                }
            )
            ToolbarActionButton(
                icon = Icons.Default.ChevronRight,
                contentDescription = "Move Right",
                tooltip = if (canMoveRight) "Move Right" else null,
                enabled = canMoveRight,
                onClick = {
                    onActivate()
                    onMoveRight()
                }
            )

            // Separator 2: Reorder | Actions
            VerticalDivider(modifier = Modifier.height(18.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Actions Group: Rename | Duplicate | Delete
            ToolbarActionButton(
                icon = Icons.Default.Edit,
                contentDescription = "Rename Screen",
                tooltip = "Rename Screen",
                onClick = {
                    onActivate()
                    showRenameDialog = true
                }
            )
            ToolbarActionButton(
                icon = Icons.Default.ContentCopy,
                contentDescription = "Duplicate Screen",
                tooltip = "Duplicate Screen",
                onClick = {
                    onActivate()
                    onDuplicate()
                }
            )
            ToolbarActionButton(
                icon = Icons.Default.Delete,
                contentDescription = "Delete Screen",
                tooltip = if (canDeleteScreen) "Delete Screen" else "Can't delete the last screen",
                enabled = canDeleteScreen,
                tint = MaterialTheme.colorScheme.error,
                disabledTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                onClick = {
                    onActivate()
                    onDelete()
                }
            )

            if (showRenameDialog) {
                var newNameText by remember { mutableStateOf(screen.name) }
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = { Text("Rename Screen") },
                    text = {
                        OutlinedTextField(
                            value = newNameText,
                            onValueChange = { newNameText = it },
                            label = { Text("Screen Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showRenameDialog = false
                                onRename(newNameText)
                            }
                        ) {
                            Text("Rename")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolbarActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(32.dp),
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    disabledTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    tooltip: String? = null
) {
    val button = @Composable {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = if (enabled) tint else disabledTint
            )
        }
    }

    if (tooltip != null) {
        TooltipArea(
            delayMillis = 500,
            tooltip = {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = tooltip,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            },
            tooltipPlacement = TooltipPlacement.ComponentRect(
                anchor = Alignment.TopCenter,
                alignment = Alignment.TopCenter,
                offset = DpOffset(x = 0.dp, y = (-8).dp)
            )
        ) {
            button()
        }
    } else {
        button()
    }
}
