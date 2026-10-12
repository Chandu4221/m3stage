package io.github.chandu4221.m3stage.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.materialkolor.material3.DynamicMaterialTheme

@Composable
fun AppTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    DynamicMaterialTheme(
        seedColor = SeedColorPresets.RosePink.color,
        isDark = isDark,
        content = content,
        typography = createRobotoTypography()
    )
}