package io.github.chandu4221.m3stage.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.theme.SeedColorPresets

@Composable
fun TopProjectAppBar(
    project: Project?,
    activeScreenId: ScreenId?,
    store: EditorStore,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var seedDropdownExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to newly active screen
    LaunchedEffect(activeScreenId) {
        val index = project?.screens?.indexOfFirst { it.id == activeScreenId } ?: -1
        if (index != -1) {
            listState.animateScrollToItem(index)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ==========================================
            // TIER 1: App Title, Project Name, Actions
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: App Brand
                Text(
                    text = "m3stage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Center: Project Name
                Text(
                    text = project?.name ?: "Untitled Project",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.weight(1f))

                // Right: Actions (Theme Seed, Dark Mode, Add screen, Export)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seed Color Picker Dropdown
                    Box {
                        OutlinedButton(
                            onClick = { seedDropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(project?.seedColor ?: 0xFF6750A4L))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Theme", style = MaterialTheme.typography.labelMedium)
                        }

                        DropdownMenu(
                            expanded = seedDropdownExpanded,
                            onDismissRequest = { seedDropdownExpanded = false }
                        ) {
                            SeedColorPresets.all.forEach { preset ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(preset.color)
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                    },
                                    text = { Text(preset.name, style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        store.updateSeedColor(preset.argb)
                                        seedDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Dark/Light Mode Toggle Button
                    IconButton(
                        onClick = { store.toggleDarkMode() },
                    ) {
                        val isDark = project?.isDarkMode ?: false
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    VerticalDivider(modifier = Modifier.height(20.dp))

                    // Add Screen Button
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add screen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add screen", style = MaterialTheme.typography.labelMedium)
                    }

                    // Export Screens Button
                    Button(
                        onClick = { store.exportCode() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export Screens",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Screens", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // ==========================================
            // TIER 2: Scrollable Screens LazyRow
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val screens = project?.screens ?: emptyList()
                    items(screens, key = { it.id.value }) { screen ->
                        val isActive = screen.id == activeScreenId

                        FilterChip(
                            selected = isActive,
                            onClick = { store.setActiveScreen(screen.id) },
                            label = {
                                Text(
                                    text = screen.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isActive,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = if (isActive) 1.5.dp else 1.dp
                            )
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // Add Screen Modal Dialog
    // ==========================================
    if (showAddDialog) {
        var screenName by remember { mutableStateOf("") }
        var route by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Screen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = screenName,
                        onValueChange = {
                            screenName = it
                            if (route.isEmpty() || route.startsWith("/")) {
                                route = "/${it.trim().lowercase().replace(" ", "-")}"
                            }
                        },
                        label = { Text("Screen Name") },
                        placeholder = { Text("e.g. Profile, Settings") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = route,
                        onValueChange = { route = it },
                        label = { Text("Route") },
                        placeholder = { Text("e.g. /profile") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = screenName.trim().ifEmpty { "Screen ${(project?.screens?.size ?: 0) + 1}" }
                        val r = route.trim().ifEmpty { "/${name.lowercase()}" }
                        store.addNewScreen(name = name, route = r)
                        showAddDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}