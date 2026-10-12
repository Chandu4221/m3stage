package io.github.chandu4221.m3stage.ui.organism

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import io.github.chandu4221.m3stage.component.ComponentKind
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportState
import io.github.chandu4221.m3stage.ui.device.DeviceFrame
import io.github.chandu4221.m3stage.ui.device.DeviceOrientation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CanvasPanel(
    screens: List<Screen>,
    activeScreenId: ScreenId?,
    onSelectScreen: (ScreenId) -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId) -> Unit,
    viewportState: CanvasViewportState,
    onPanDelta: (Offset) -> Unit,
    onWheelZoom: (Float) -> Unit,
    lockedNodeIds: Set<NodeId> = emptySet(),
    projectDefaultDevice: DevicePreset = DevicePreset.Default,
    onRenameScreen: (ScreenId, String) -> Unit = { _, _ -> },
    onDeleteScreen: (ScreenId) -> Unit = {},
    onDuplicateScreen: (ScreenId) -> Unit = {},
    onReorderScreens: (Int, Int) -> Unit = { _, _ -> },
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // --- INFINITE TRANSFORMABLE STUDIO WORKBENCH ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceDim)
            // 1. Mouse Dragging (Pans when in Pan tool mode or middle mouse drag)
            .pointerInput(viewportState.activeTool) {
                detectDragGestures { change, dragAmount ->
                    if (viewportState.activeTool == CanvasPointerTool.Pan) {
                        change.consume()
                        onPanDelta(dragAmount)
                    }
                }
            }
            // 2. Mouse Wheel Scroll (Ctrl+Scroll zooms, plain scroll pans)
            .onPointerEvent(PointerEventType.Scroll) { event ->
                val change = event.changes.firstOrNull() ?: return@onPointerEvent
                val deltaY = change.scrollDelta.y
                val isCtrlPressed = event.keyboardModifiers.isCtrlPressed

                if (isCtrlPressed) {
                    val zoomDelta = if (deltaY < 0) 0.1f else -0.1f
                    onWheelZoom(zoomDelta)
                } else {
                    val deltaX = change.scrollDelta.x
                    onPanDelta(Offset(-deltaX * 20f, -deltaY * 20f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // --- TRANSFORMABLE ARTBOARDS CONTAINER ---
        Box(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .graphicsLayer {
                    scaleX = viewportState.zoom
                    scaleY = viewportState.zoom
                    translationX = viewportState.panOffset.x
                    translationY = viewportState.panOffset.y
                }
                .padding(vertical = 56.dp, horizontal = 72.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.Top
            ) {
                screens.forEachIndexed { index, screen ->
                    val isActive = screen.id == activeScreenId
                    ScreenArtboard(
                        screen = screen,
                        isActive = isActive,
                        onActivate = { onSelectScreen(screen.id) },
                        selectedNodeId = if (isActive) selectedNodeId else null,
                        onNodeClick = { nodeId ->
                            onSelectScreen(screen.id)
                            onNodeClick(nodeId)
                        },
                        lockedNodeIds = lockedNodeIds,
                        projectDefaultDevice = projectDefaultDevice,
                        canMoveLeft = index > 0,
                        canMoveRight = index < screens.lastIndex,
                        onMoveLeft = { onReorderScreens(index, index - 1) },
                        onMoveRight = { onReorderScreens(index, index + 1) },
                        onRename = { newName -> onRenameScreen(screen.id, newName) },
                        onDuplicate = { onDuplicateScreen(screen.id) },
                        onDelete = { onDeleteScreen(screen.id) },
                        draggedComponentKind = draggedComponentKind,
                        onDropOnScreen = onDropOnScreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenArtboard(
    screen: Screen,
    isActive: Boolean,
    onActivate: () -> Unit,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId) -> Unit,
    lockedNodeIds: Set<NodeId>,
    projectDefaultDevice: DevicePreset,
    canMoveLeft: Boolean = false,
    canMoveRight: Boolean = false,
    onMoveLeft: () -> Unit = {},
    onMoveRight: () -> Unit = {},
    onRename: (String) -> Unit = {},
    onDuplicate: () -> Unit = {},
    onDelete: () -> Unit = {},
    draggedComponentKind: ComponentKind? = null,
    onDropOnScreen: (ScreenId, ComponentKind) -> Unit = { _, _ -> }
) {
    var activeDevice by remember(screen.id) {
        mutableStateOf(screen.resolveDevice(projectDefaultDevice))
    }
    var orientation by remember(screen.id) { mutableStateOf(DeviceOrientation.Portrait) }
    var showFrame by remember { mutableStateOf(true) }
    var deviceDropdownExpanded by remember { mutableStateOf(false) }

    val currentW = if (orientation == DeviceOrientation.Portrait) activeDevice.widthDp else activeDevice.heightDp
    val currentH = if (orientation == DeviceOrientation.Portrait) activeDevice.heightDp else activeDevice.widthDp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics { role = Role.Tab }
    ) {
        // --- FLOATING ARTBOARD CONTROLS ---
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
                                    activeDevice = preset
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
                        orientation = if (orientation == DeviceOrientation.Portrait)
                            DeviceOrientation.Landscape else DeviceOrientation.Portrait
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
                        showFrame = it
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
                        text = "$currentW × $currentH dp",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                VerticalDivider(modifier = Modifier.height(18.dp))

                // Screen Actions Menu Button (Rename, Duplicate, Move, Delete)
                var actionsMenuExpanded by remember { mutableStateOf(false) }
                var showRenameDialog by remember { mutableStateOf(false) }

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

        // --- HARDWARE FRAME & LIVE COMPOSE RENDERER ---
        Box(
            modifier = Modifier
                .clickable(onClick = {
                    if (draggedComponentKind != null) {
                        onDropOnScreen(screen.id, draggedComponentKind)
                    } else {
                        onActivate()
                    }
                })
                .then(
                    if (draggedComponentKind != null) {
                        Modifier.drawBehind {
                            val strokeWidth = 3.dp.toPx()
                            drawRoundRect(
                                color = androidx.compose.ui.graphics.Color(0xFF388E3C), // Drop target green highlight
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                                style = Stroke(
                                    width = strokeWidth,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                )
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            DeviceFrame(
                preset = activeDevice,
                orientation = orientation,
                showFrame = showFrame
            ) {
                DesignRenderer.Render(
                    node = screen.root,
                    selectedNodeId = selectedNodeId,
                    onNodeClick = onNodeClick,
                    lockedNodeIds = lockedNodeIds
                )
            }
        }
    }
}

@Composable
private fun AddScreenGhostCard(onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val cornerRadius = 16.dp

    Box(
        modifier = Modifier
            .padding(top = 44.dp)
            .size(width = 240.dp, height = 480.dp)
            .drawBehind {
                val strokeWidth = 1.5.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawRoundRect(
                    color = outlineColor,
                    style = Stroke(width = strokeWidth, pathEffect = dashEffect),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(onClick = onClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Screen")
            }
            Text(
                text = "Add New Screen",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}