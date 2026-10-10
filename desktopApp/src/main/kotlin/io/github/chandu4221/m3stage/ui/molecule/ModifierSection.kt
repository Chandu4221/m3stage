package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.ModifierNode
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.theme.M3ColorToken

@Composable
fun ModifierSection(
    node: DesignNode,
    isLocked: Boolean,
    store: EditorStore,
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Title + Add Modifier Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Modifiers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (node.modifiers.isNotEmpty()) {
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = "${node.modifiers.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { showAddMenu = true },
                        enabled = !isLocked,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Modifier",
                            tint = if (!isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Padding") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Padding(16f, 16f, 16f, 16f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fill Max Width") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.FillMaxWidth())
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fill Max Height") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.FillMaxHeight())
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fill Max Size") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.FillMaxSize())
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Size (W x H)") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Size(100f, 100f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Width") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Width(100f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Height") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Height(100f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Background") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierNode.Background(PropertyValue.ColorValue.Token(M3ColorToken.PrimaryContainer))
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Border") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierNode.Border(
                                        width = 1f,
                                        color = PropertyValue.ColorValue.Token(M3ColorToken.Outline),
                                        shape = PropertyValue.ShapeValue.UniformDp(8f)
                                    )
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clip") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierNode.Clip(PropertyValue.ShapeValue.UniformDp(8f))
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shadow") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierNode.Shadow(
                                        elevation = 4f,
                                        shape = PropertyValue.ShapeValue.UniformDp(8f)
                                    )
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Alpha (Opacity)") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Alpha(0.8f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clickable") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Clickable(true))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Offset") },
                            onClick = {
                                store.addModifier(node.id, ModifierNode.Offset(0f, 0f))
                                showAddMenu = false
                            }
                        )
                    }
                }
            }

            if (node.modifiers.isEmpty()) {
                Text(
                    text = "No modifiers added yet. Click + to customize size, padding, borders, etc.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                node.modifiers.forEachIndexed { index, modNode ->
                    ModifierItemCard(
                        mod = modNode,
                        index = index,
                        totalCount = node.modifiers.size,
                        isLocked = isLocked,
                        onUpdate = { updatedMod ->
                            store.updateModifier(node.id, index, updatedMod)
                        },
                        onRemove = {
                            store.removeModifier(node.id, index)
                        },
                        onMoveUp = {
                            if (index > 0) store.reorderModifier(node.id, index, index - 1)
                        },
                        onMoveDown = {
                            if (index < node.modifiers.size - 1) store.reorderModifier(node.id, index, index + 1)
                        }
                    )
                    if (index < node.modifiers.size - 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModifierItemCard(
    mod: ModifierNode,
    index: Int,
    totalCount: Int,
    isLocked: Boolean,
    onUpdate: (ModifierNode) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header Row: Mod Name + Move/Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${index + 1}. ${getModifierTitle(mod)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Move Up
                    IconButton(
                        onClick = onMoveUp,
                        enabled = !isLocked && index > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Move Down
                    IconButton(
                        onClick = onMoveDown,
                        enabled = !isLocked && index < totalCount - 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Delete
                    IconButton(
                        onClick = onRemove,
                        enabled = !isLocked,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Modifier",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Inline Property Editor based on Modifier Type
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                when (mod) {
                    is ModifierNode.Padding -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            NumberField(label = "Start", value = mod.start, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(start = it))
                            }
                            NumberField(label = "Top", value = mod.top, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(top = it))
                            }
                            NumberField(label = "End", value = mod.end, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(end = it))
                            }
                            NumberField(label = "Bottom", value = mod.bottom, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(bottom = it))
                            }
                        }
                    }

                    is ModifierNode.FillMaxWidth -> {
                        SliderField(label = "Fraction", value = mod.fraction) {
                            onUpdate(mod.copy(fraction = it))
                        }
                    }

                    is ModifierNode.FillMaxHeight -> {
                        SliderField(label = "Fraction", value = mod.fraction) {
                            onUpdate(mod.copy(fraction = it))
                        }
                    }

                    is ModifierNode.FillMaxSize -> {
                        SliderField(label = "Fraction", value = mod.fraction) {
                            onUpdate(mod.copy(fraction = it))
                        }
                    }

                    is ModifierNode.Size -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NumberField(label = "Width (dp)", value = mod.width, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(width = it))
                            }
                            NumberField(label = "Height (dp)", value = mod.height, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(height = it))
                            }
                        }
                    }

                    is ModifierNode.Width -> {
                        NumberField(label = "Width (dp)", value = mod.width) {
                            onUpdate(mod.copy(width = it))
                        }
                    }

                    is ModifierNode.Height -> {
                        NumberField(label = "Height (dp)", value = mod.height) {
                            onUpdate(mod.copy(height = it))
                        }
                    }

                    is ModifierNode.WrapContentSize -> {
                        Text(
                            text = "Allows content to measure at its desired size.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is ModifierNode.Alpha -> {
                        SliderField(label = "Alpha", value = mod.alpha) {
                            onUpdate(mod.copy(alpha = it))
                        }
                    }

                    is ModifierNode.Clickable -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enabled", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = mod.enabled,
                                onCheckedChange = { onUpdate(mod.copy(enabled = it)) },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }

                    is ModifierNode.Offset -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NumberField(label = "X (dp)", value = mod.x, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(x = it))
                            }
                            NumberField(label = "Y (dp)", value = mod.y, modifier = Modifier.weight(1f)) {
                                onUpdate(mod.copy(y = it))
                            }
                        }
                    }

                    is ModifierNode.Background -> {
                        Text(
                            text = "Color: ${mod.color}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is ModifierNode.Border -> {
                        NumberField(label = "Border Width (dp)", value = mod.width) {
                            onUpdate(mod.copy(width = it))
                        }
                    }

                    is ModifierNode.Clip -> {
                        Text(
                            text = "Shape: ${mod.shape}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is ModifierNode.Shadow -> {
                        NumberField(label = "Elevation (dp)", value = mod.elevation) {
                            onUpdate(mod.copy(elevation = it))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Float,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit
) {
    OutlinedTextField(
        value = if (value % 1f == 0f) value.toInt().toString() else value.toString(),
        onValueChange = { str ->
            str.toFloatOrNull()?.let(onValueChange)
        },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun SliderField(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f
        )
    }
}

private fun getModifierTitle(mod: ModifierNode): String = when (mod) {
    is ModifierNode.Padding -> "Padding"
    is ModifierNode.FillMaxWidth -> "Fill Max Width"
    is ModifierNode.FillMaxHeight -> "Fill Max Height"
    is ModifierNode.FillMaxSize -> "Fill Max Size"
    is ModifierNode.Size -> "Size"
    is ModifierNode.Width -> "Width"
    is ModifierNode.Height -> "Height"
    is ModifierNode.WrapContentSize -> "Wrap Content Size"
    is ModifierNode.Background -> "Background"
    is ModifierNode.Border -> "Border"
    is ModifierNode.Clip -> "Clip"
    is ModifierNode.Shadow -> "Shadow"
    is ModifierNode.Alpha -> "Alpha"
    is ModifierNode.Clickable -> "Clickable"
    is ModifierNode.Offset -> "Offset"
}