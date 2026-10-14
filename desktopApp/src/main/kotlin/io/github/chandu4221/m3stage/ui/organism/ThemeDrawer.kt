package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.resolver.ThemeResolver
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview
import io.github.chandu4221.m3stage.ui.theme.SeedColorPreset
import io.github.chandu4221.m3stage.ui.theme.SeedColorPresets

enum class ThemeTab {
    Palettes,
    Custom
}

/**
 * Organism: Colors & Theme drawer panel.
 * Displays brightness control, curated Material 3 seed palettes with dynamic tonal chip previews,
 * and custom Hex seed color input.
 */
@Composable
fun ThemeDrawer(
    currentSeedColor: Long,
    isDarkMode: Boolean,
    onSelectSeedColor: (Long) -> Unit,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier.width(280.dp)
) {
    var activeTab by remember { mutableStateOf(ThemeTab.Palettes) }

    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Colors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Brightness Toggle Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = onToggleDarkMode
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Brightness",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Brightness",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isDarkMode) "Dark" else "Light",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Toggle brightness",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Segmented Tabs: Palettes vs Custom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeTab == ThemeTab.Palettes) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    onClick = { activeTab = ThemeTab.Palettes }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = if (activeTab == ThemeTab.Palettes) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Palettes",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeTab == ThemeTab.Palettes) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeTab == ThemeTab.Custom) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    onClick = { activeTab = ThemeTab.Custom }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (activeTab == ThemeTab.Custom) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Custom",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeTab == ThemeTab.Custom) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // 4. Tab Content
            when (activeTab) {
                ThemeTab.Palettes -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(SeedColorPresets.all, key = { it.argb }) { preset ->
                            val isSelected = preset.argb == currentSeedColor
                            PresetPaletteCard(
                                preset = preset,
                                isSelected = isSelected,
                                isDarkMode = isDarkMode,
                                onClick = { onSelectSeedColor(preset.argb) }
                            )
                        }
                    }
                }

                ThemeTab.Custom -> {
                    CustomColorView(
                        currentSeedColor = currentSeedColor,
                        isDarkMode = isDarkMode,
                        onApply = onSelectSeedColor
                    )
                }
            }
        }
    }
}

/**
 * Selectable card displaying a seed color preset with split-color badge and tonal palette chips.
 */
@Composable
private fun PresetPaletteCard(
    preset: SeedColorPreset,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presetScheme = remember(preset.argb, isDarkMode) {
        ThemeResolver.resolveScheme(preset.argb, isDarkMode)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = if (isSelected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                SplitSeedBadge(
                    primaryColor = presetScheme.primary,
                    containerColor = presetScheme.primaryContainer
                )
                Text(
                    text = preset.shortName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TonalPaletteChips(scheme = presetScheme)
        }
    }
}

/**
 * Circular split-color badge dividing primary and primaryContainer along the diagonal.
 */
@Composable
fun SplitSeedBadge(
    primaryColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
    ) {
        // Top-left half: 135 deg to 315 deg (180 deg sweep)
        drawArc(
            color = primaryColor,
            startAngle = 135f,
            sweepAngle = 180f,
            useCenter = true
        )
        // Bottom-right half: 315 deg to 135 deg (180 deg sweep)
        drawArc(
            color = containerColor,
            startAngle = 315f,
            sweepAngle = 180f,
            useCenter = true
        )
    }
}

/**
 * Row of 4 overlapping tonal chips (Primary, Secondary, Tertiary, SurfaceContainerHigh).
 */
@Composable
fun TonalPaletteChips(
    scheme: ColorScheme,
    modifier: Modifier = Modifier
) {
    val chips = remember(scheme) {
        listOf(
            scheme.primary,
            scheme.secondary,
            scheme.tertiary,
            scheme.surfaceContainerHigh
        )
    }

    Box(
        modifier = modifier
            .width((18 + (chips.size - 1) * 12).dp)
            .height(18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        chips.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .offset(x = (index * 12).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

/**
 * Custom color input view allowing direct HEX code entry with validation and preview.
 */
@Composable
private fun CustomColorView(
    currentSeedColor: Long,
    isDarkMode: Boolean,
    onApply: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var hexText by remember {
        mutableStateOf(String.format("%06X", currentSeedColor and 0xFFFFFFL))
    }
    val parsedColor = remember(hexText) { parseHexColor(hexText) }
    val isValid = parsedColor != null

    val previewScheme = remember(parsedColor, isDarkMode) {
        parsedColor?.let { ThemeResolver.resolveScheme(it, isDarkMode) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Custom Seed Color",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = hexText,
            onValueChange = { hexText = it.take(8).uppercase() },
            label = { Text("Hex Code") },
            placeholder = { Text("6750A4") },
            prefix = { Text("#") },
            singleLine = true,
            isError = hexText.isNotBlank() && !isValid,
            trailingIcon = {
                if (parsedColor != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(parsedColor))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (hexText.isNotBlank() && !isValid) {
            Text(
                text = "Enter a valid 6-character hex code (e.g. 6750A4)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Live preview of the custom scheme if valid
        if (previewScheme != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Generated M3 Roles Preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SplitSeedBadge(
                            primaryColor = previewScheme.primary,
                            containerColor = previewScheme.primaryContainer
                        )
                        TonalPaletteChips(scheme = previewScheme)
                    }
                }
            }
        }

        Button(
            onClick = {
                if (parsedColor != null) {
                    onApply(parsedColor)
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Seed Color")
        }
    }
}

/**
 * Maps presets to concise, clean display names matching the Theme Builder reference.
 */
fun SeedColorPreset.shortName(): String = when (this) {
    SeedColorPresets.BaselinePurple -> "Purple"
    SeedColorPresets.SapphireBlue -> "Blue"
    SeedColorPresets.EmeraldGreen -> "Green"
    SeedColorPresets.CoralRed -> "Coral"
    SeedColorPresets.SunsetOrange -> "Amber"
    SeedColorPresets.OceanTeal -> "Teal"
    SeedColorPresets.RosePink -> "Pink"
    SeedColorPresets.SlateMono -> "Mono"
    else -> name
}

/**
 * Parses hex color strings (#RRGGBB, RRGGBB, #AARRGGBB, AARRGGBB) to a 32-bit ARGB Long.
 */
fun parseHexColor(input: String): Long? {
    val clean = input.trim().removePrefix("#").removePrefix("0x").removePrefix("0X")
    return when (clean.length) {
        6 -> {
            val rgb = clean.toLongOrNull(16) ?: return null
            0xFF000000L or rgb
        }
        8 -> clean.toLongOrNull(16)
        else -> null
    }
}

@Preview
@Composable
private fun ThemeDrawerPreview() {
    DualThemePreview {
        var seed by remember { mutableStateOf(0xFF6750A4L) }
        var isDark by remember { mutableStateOf(false) }

        ThemeDrawer(
            currentSeedColor = seed,
            isDarkMode = isDark,
            onSelectSeedColor = { seed = it },
            onToggleDarkMode = { isDark = !isDark }
        )
    }
}
