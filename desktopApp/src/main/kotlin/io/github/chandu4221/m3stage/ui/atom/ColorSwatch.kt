package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Atom: Circular color swatch with selection checkmark.
 */
@Composable
fun ColorSwatch(
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    contentDescription: String = "Color swatch"
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                CircleShape
            )
            .semantics { role = Role.Button }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Determine contrasting icon color based on swatch luminance
            val isColorDark = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue) < 0.5
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "$contentDescription selected",
                tint = if (isColorDark) Color.White else Color.Black,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}


@Preview
@Composable
private fun ColorSwatchPreview() {
    DualThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorSwatch(color = Color.Red)
        }
    }
}