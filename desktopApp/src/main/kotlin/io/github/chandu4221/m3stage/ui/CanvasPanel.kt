package io.github.chandu4221.m3stage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.ui.device.DeviceFrame
import io.github.chandu4221.m3stage.ui.device.DeviceOrientation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasPanel(
    screen: Screen,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId) -> Unit,
    lockedNodeIds: Set<NodeId> = emptySet(),
    projectDefaultDevice: DevicePreset = DevicePreset.Default
) {
    // Active device preset (defaults to screen device or project default)
    var activeDevice by remember(screen.id) {
        mutableStateOf(screen.resolveDevice(projectDefaultDevice))
    }
    var orientation by remember { mutableStateOf(DeviceOrientation.Portrait) }
    var showFrame by remember { mutableStateOf(true) }
    var deviceDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- TOP TOOLBAR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Screen Title
                Text(
                    text = "Screen: ${screen.name}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                VerticalDivider(modifier = Modifier.height(20.dp))

                // Device Preset Dropdown
                Box {
                    OutlinedButton(
                        onClick = { deviceDropdownExpanded = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Device",
                            modifier = Modifier.size(16.dp)
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
                                            "${preset.widthDp} × ${preset.heightDp} dp (${preset.category.displayName})",
                                            style = MaterialTheme.typography.labelSmall,
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

                // Orientation Toggle (Portrait <-> Landscape)
                IconButton(
                    onClick = {
                        orientation = if (orientation == DeviceOrientation.Portrait) {
                            DeviceOrientation.Landscape
                        } else {
                            DeviceOrientation.Portrait
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (orientation == DeviceOrientation.Portrait) Icons.Default.CropPortrait else Icons.Default.CropLandscape,
                        contentDescription = "Toggle Orientation",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Frame Toggle (Show Bezel on/off)
                FilterChip(
                    selected = showFrame,
                    onClick = { showFrame = !showFrame },
                    label = { Text("Device Frame", style = MaterialTheme.typography.labelSmall) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Dimensions Badge
                val currentW =
                    if (orientation == DeviceOrientation.Portrait) activeDevice.widthDp else activeDevice.heightDp
                val currentH =
                    if (orientation == DeviceOrientation.Portrait) activeDevice.heightDp else activeDevice.widthDp
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "$currentW × $currentH dp",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- SCROLLABLE CANVAS AREA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8EAED)) // Clean neutral studio gray
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            DeviceFrame(
                preset = activeDevice,
                orientation = orientation,
                showFrame = showFrame
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
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
}