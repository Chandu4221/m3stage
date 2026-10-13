package io.github.chandu4221.m3stage.ui.canvas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onSelectScreen: (ScreenId) -> Unit,
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
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.Top
            ) {
                screens.forEachIndexed { index, screen ->
                    val isActive = screen.id == activeScreenId
                    var activeDevice by remember(screen.id) {
                        mutableStateOf(screen.resolveDevice(projectDefaultDevice))
                    }
                    var orientation by remember(screen.id) { mutableStateOf(DeviceOrientation.Portrait) }
                    var showFrame by remember { mutableStateOf(true) }

                    val currentW = if (orientation == DeviceOrientation.Portrait) activeDevice.widthDp else activeDevice.heightDp
                    val currentH = if (orientation == DeviceOrientation.Portrait) activeDevice.heightDp else activeDevice.widthDp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // P5: Floating Artboard Toolbar (Counter-scaled at 1 / zoom)
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
                                onActivate = { onSelectScreen(screen.id) },
                                onDeviceChange = { activeDevice = it },
                                onOrientationToggle = {
                                    orientation = if (orientation == DeviceOrientation.Portrait)
                                        DeviceOrientation.Landscape else DeviceOrientation.Portrait
                                },
                                onFrameToggle = { showFrame = it },
                                canMoveLeft = canMoveLeft(index),
                                canMoveRight = canMoveRight(index),
                                onMoveLeft = { onMoveLeft(index) },
                                onMoveRight = { onMoveRight(index) },
                                onRename = { onRenameScreen(screen.id, it) },
                                onDuplicate = { onDuplicateScreen(screen.id) },
                                onDelete = { onDeleteScreen(screen.id) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingArtboardToolbar(
    screen: Screen,
    isActive: Boolean,
    activeDevice: DevicePreset,
    orientation: DeviceOrientation,
    showFrame: Boolean,
    widthDp: Int,
    heightDp: Int,
    onActivate: () -> Unit,
    onDeviceChange: (DevicePreset) -> Unit,
    onOrientationToggle: () -> Unit,
    onFrameToggle: (Boolean) -> Unit,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRename: (String) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var deviceDropdownExpanded by remember { mutableStateOf(false) }
    var actionsMenuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = if (isActive) 4.dp else 2.dp,
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.clickable(onClick = onActivate)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Screen Name Badge
            Text(
                text = screen.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            VerticalDivider(modifier = Modifier.height(18.dp))

            // Device Preset Dropdown
            Box {
                FilledTonalButton(
                    onClick = {
                        onActivate()
                        deviceDropdownExpanded = true
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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
            IconButton(
                onClick = {
                    onActivate()
                    onOrientationToggle()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (orientation == DeviceOrientation.Portrait)
                        Icons.Default.CropPortrait else Icons.Default.CropLandscape,
                    contentDescription = "Toggle Orientation",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Hardware Bezel Toggle
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

            VerticalDivider(modifier = Modifier.height(18.dp))

            // Screen Actions Menu
            Box {
                IconButton(
                    onClick = {
                        onActivate()
                        actionsMenuExpanded = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Screen actions",
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = actionsMenuExpanded,
                    onDismissRequest = { actionsMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename Screen") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            actionsMenuExpanded = false
                            showRenameDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate Screen") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            actionsMenuExpanded = false
                            onDuplicate()
                        }
                    )
                    if (canMoveLeft) {
                        DropdownMenuItem(
                            text = { Text("Move Left") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                actionsMenuExpanded = false
                                onMoveLeft()
                            }
                        )
                    }
                    if (canMoveRight) {
                        DropdownMenuItem(
                            text = { Text("Move Right") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                actionsMenuExpanded = false
                                onMoveRight()
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete Screen", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp)) },
                        onClick = {
                            actionsMenuExpanded = false
                            onDelete()
                        }
                    )
                }
            }

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
