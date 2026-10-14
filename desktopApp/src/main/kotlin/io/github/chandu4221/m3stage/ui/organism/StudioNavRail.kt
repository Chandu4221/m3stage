package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.atom.ToolIconButton
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

enum class StudioRailTab(val title: String, val icon: ImageVector) {
    Parts("Components", Icons.Default.AddBox),
    Layers("Layers & Tree", Icons.Default.AccountTree),
    Theme("Color & Theme", Icons.Default.Palette),
    Settings("Project Settings", Icons.Default.Settings)
}

/**
 * Dumb Organism: Compact far-left studio navigation rail.
 * Pure inputs: activeTab. Pure outputs: onTabSelected.
 */
@Composable
fun StudioNavRail(
    activeTab: StudioRailTab,
    onTabSelected: (StudioRailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(60.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Branding Icon
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "m3stage Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(28.dp)
                    .padding(bottom = 8.dp)
            )

            StudioRailTab.entries.forEach { tab ->
                ToolIconButton(
                    icon = tab.icon,
                    contentDescription = tab.title,
                    isSelected = tab == activeTab,
                    onClick = { onTabSelected(tab) },
                    tooltip = tab.title
                )
            }
        }
    }
}

@Preview
@Composable
private fun StudioNavRailPreview() {
    DualThemePreview {
        var tab by remember { mutableStateOf(StudioRailTab.Parts) }

        Box(modifier = Modifier.height(380.dp)) {
            StudioNavRail(
                activeTab = tab,
                onTabSelected = { tab = it }
            )
        }
    }
}