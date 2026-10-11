package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.modifier.ModifierSpec
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
                        // 1. Padding
                        DropdownMenuItem(
                            text = { Text("Padding") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Padding(16f, 16f, 16f, 16f))
                                showAddMenu = false
                            }
                        )
                        // 2. Size
                        DropdownMenuItem(
                            text = { Text("Fill Max Width") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.FillMaxWidth())
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fill Max Height") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.FillMaxHeight())
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Fill Max Size") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.FillMaxSize())
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Size (W x H)") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Size(100f, 100f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Width") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Width(100f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Height") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Height(100f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Weight") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Weight(1f, fill = true))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Aspect Ratio") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.AspectRatio(1f))
                                showAddMenu = false
                            }
                        )
                        // 3. Position & Order
                        DropdownMenuItem(
                            text = { Text("Offset") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Offset(0f, 0f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Z-Index") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.ZIndex(1f))
                                showAddMenu = false
                            }
                        )
                        // 4. Drawing & Border
                        DropdownMenuItem(
                            text = { Text("Background") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierSpec.Background(PropertyValue.ColorValue.Token(M3ColorToken.PrimaryContainer))
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Border") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierSpec.Border(
                                        width = 1f,
                                        color = PropertyValue.ColorValue.Token(M3ColorToken.Outline),
                                        shape = PropertyValue.ShapeValue.UniformDp(8f)
                                    )
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Shadow") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierSpec.Shadow(
                                        elevation = 4f,
                                        shape = PropertyValue.ShapeValue.UniformDp(8f)
                                    )
                                )
                                showAddMenu = false
                            }
                        )
                        // 5. Graphics Layer
                        DropdownMenuItem(
                            text = { Text("Clip") },
                            onClick = {
                                store.addModifier(
                                    node.id,
                                    ModifierSpec.Clip(PropertyValue.ShapeValue.UniformDp(8f))
                                )
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Alpha (Opacity)") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Alpha(0.8f))
                                showAddMenu = false
                            }
                        )
                        // 6. Transformations
                        DropdownMenuItem(
                            text = { Text("Rotate") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Rotate(0f))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Scale") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.Scale(1f))
                                showAddMenu = false
                            }
                        )
                        // 7. Scroll
                        DropdownMenuItem(
                            text = { Text("Vertical Scroll") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.VerticalScroll(true))
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Horizontal Scroll") },
                            onClick = {
                                store.addModifier(node.id, ModifierSpec.HorizontalScroll(true))
                                showAddMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Modifier Cards List
            if (node.modifiers.isEmpty()) {
                Text(
                    text = "No visual modifiers attached.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                node.modifiers.forEachIndexed { index, mod ->
                    ModifierCard(
                        modifier = mod,
                        isLocked = isLocked,
                        onUpdate = { updatedMod ->
                            store.updateModifier(node.id, index, updatedMod)
                        },
                        onRemove = {
                            store.removeModifier(node.id, index)
                        }
                    )
                    if (index < node.modifiers.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModifierCard(
    modifier: ModifierSpec,
    isLocked: Boolean,
    onUpdate: (ModifierSpec) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getModifierTitle(modifier),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onRemove,
                    enabled = !isLocked,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove Modifier",
                        tint = if (!isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f
                        ),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Inline Property Editor based on Modifier Type
            when (modifier) {
                // 1. Padding
                is ModifierSpec.Padding -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NumberField(label = "Start", value = modifier.start, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(start = it))
                        }
                        NumberField(label = "Top", value = modifier.top, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(top = it))
                        }
                        NumberField(label = "End", value = modifier.end, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(end = it))
                        }
                        NumberField(label = "Bottom", value = modifier.bottom, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(bottom = it))
                        }
                    }
                }

                // 2. Size
                is ModifierSpec.FillMaxWidth -> {
                    SliderField(label = "Fraction", value = modifier.fraction) {
                        onUpdate(modifier.copy(fraction = it))
                    }
                }

                is ModifierSpec.FillMaxHeight -> {
                    SliderField(label = "Fraction", value = modifier.fraction) {
                        onUpdate(modifier.copy(fraction = it))
                    }
                }

                is ModifierSpec.FillMaxSize -> {
                    SliderField(label = "Fraction", value = modifier.fraction) {
                        onUpdate(modifier.copy(fraction = it))
                    }
                }

                is ModifierSpec.Size -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NumberField(label = "Width (dp)", value = modifier.width, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(width = it))
                        }
                        NumberField(label = "Height (dp)", value = modifier.height, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(height = it))
                        }
                    }
                }

                is ModifierSpec.Width -> {
                    NumberField(label = "Width (dp)", value = modifier.width) {
                        onUpdate(modifier.copy(width = it))
                    }
                }

                is ModifierSpec.Height -> {
                    NumberField(label = "Height (dp)", value = modifier.height) {
                        onUpdate(modifier.copy(height = it))
                    }
                }

                is ModifierSpec.WrapContentSize -> {
                    Text(
                        text = "Allows content to measure at its desired size.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ModifierSpec.Weight -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberField(label = "Weight", value = modifier.weight, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(weight = it))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = modifier.fill, onCheckedChange = { onUpdate(modifier.copy(fill = it)) })
                            Text("Fill", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                is ModifierSpec.AspectRatio -> {
                    NumberField(label = "Aspect Ratio", value = modifier.ratio) {
                        onUpdate(modifier.copy(ratio = it))
                    }
                }

                // 3. Position & Order
                is ModifierSpec.Offset -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NumberField(label = "X (dp)", value = modifier.x, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(x = it))
                        }
                        NumberField(label = "Y (dp)", value = modifier.y, modifier = Modifier.weight(1f)) {
                            onUpdate(modifier.copy(y = it))
                        }
                    }
                }

                is ModifierSpec.ZIndex -> {
                    NumberField(label = "Z-Index", value = modifier.zIndex) {
                        onUpdate(modifier.copy(zIndex = it))
                    }
                }

                // 4. Drawing & Border
                is ModifierSpec.Background -> {
                    Text(
                        text = "Color: ${modifier.color}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ModifierSpec.Border -> {
                    NumberField(label = "Border Width (dp)", value = modifier.width) {
                        onUpdate(modifier.copy(width = it))
                    }
                }

                is ModifierSpec.Shadow -> {
                    NumberField(label = "Elevation (dp)", value = modifier.elevation) {
                        onUpdate(modifier.copy(elevation = it))
                    }
                }

                // 5. Graphics Layer
                is ModifierSpec.Clip -> {
                    Text(
                        text = "Shape: ${modifier.shape}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is ModifierSpec.Alpha -> {
                    SliderField(label = "Alpha", value = modifier.alpha) {
                        onUpdate(modifier.copy(alpha = it))
                    }
                }

                // 6. Transformations
                is ModifierSpec.Rotate -> {
                    Slider(
                        value = modifier.degrees,
                        onValueChange = { onUpdate(modifier.copy(degrees = it)) },
                        valueRange = -180f..180f
                    )
                    Text("${modifier.degrees.toInt()}°", style = MaterialTheme.typography.bodySmall)
                }

                is ModifierSpec.Scale -> {
                    SliderField(label = "Scale", value = modifier.scale) {
                        onUpdate(modifier.copy(scale = it))
                    }
                }

                // 7. Scroll
                is ModifierSpec.VerticalScroll -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = modifier.enabled, onCheckedChange = { onUpdate(modifier.copy(enabled = it)) })
                        Text("Enable Vertical Scroll", style = MaterialTheme.typography.bodySmall)
                    }
                }

                is ModifierSpec.HorizontalScroll -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = modifier.enabled, onCheckedChange = { onUpdate(modifier.copy(enabled = it)) })
                        Text("Enable Horizontal Scroll", style = MaterialTheme.typography.bodySmall)
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

private fun getModifierTitle(mod: ModifierSpec): String = when (mod) {
    // 1. Padding
    is ModifierSpec.Padding -> "Padding"

    // 2. Size
    is ModifierSpec.FillMaxWidth -> "Fill Max Width"
    is ModifierSpec.FillMaxHeight -> "Fill Max Height"
    is ModifierSpec.FillMaxSize -> "Fill Max Size"
    is ModifierSpec.Size -> "Size"
    is ModifierSpec.Width -> "Width"
    is ModifierSpec.Height -> "Height"
    is ModifierSpec.WrapContentSize -> "Wrap Content Size"
    is ModifierSpec.Weight -> "Weight"
    is ModifierSpec.AspectRatio -> "Aspect Ratio"

    // 3. Position & Order
    is ModifierSpec.Offset -> "Offset"
    is ModifierSpec.ZIndex -> "Z-Index"

    // 4. Drawing & Border
    is ModifierSpec.Background -> "Background"
    is ModifierSpec.Border -> "Border"
    is ModifierSpec.Shadow -> "Shadow"

    // 5. Graphics Layer
    is ModifierSpec.Clip -> "Clip"
    is ModifierSpec.Alpha -> "Alpha"

    // 6. Transformations
    is ModifierSpec.Rotate -> "Rotate"
    is ModifierSpec.Scale -> "Scale"

    // 7. Scroll
    is ModifierSpec.VerticalScroll -> "Vertical Scroll"
    is ModifierSpec.HorizontalScroll -> "Horizontal Scroll"
}
