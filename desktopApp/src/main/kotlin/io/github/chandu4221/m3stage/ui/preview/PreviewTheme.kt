package io.github.chandu4221.m3stage.ui.preview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme
import io.github.chandu4221.m3stage.ui.theme.SeedColorPresets

/**
 * Renders any composable simultaneously in BOTH Light Mode and Dark Mode
 * using the real MaterialKolor dynamic color scheme.
 */
@Composable
fun DualThemePreview(
    seedColor: Color = SeedColorPresets.BaselinePurple.color,
    content: @Composable () -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // --- LIGHT THEME SECTION ---
            ThemeContainer(
                label = "Light Mode",
                isDark = false,
                seedColor = seedColor,
                content = content
            )
        }

        item {
            // --- DARK THEME SECTION ---
            ThemeContainer(
                label = "Dark Mode",
                isDark = true,
                seedColor = seedColor,
                content = content
            )
        }
    }
}

@Composable
private fun ThemeContainer(
    label: String,
    isDark: Boolean,
    seedColor: Color,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDark
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isDark) Color(0xFFB0B0B0) else Color(0xFF404040)
        )

        MaterialTheme(colorScheme = dynamicColorScheme) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}