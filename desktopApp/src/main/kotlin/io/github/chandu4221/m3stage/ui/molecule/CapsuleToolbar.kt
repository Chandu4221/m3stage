package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.atom.ToolIconButton
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Molecule: Floating capsule (island) container for toolbars and artboard controls.
 * Uses fully rounded pill styling, soft elevation, and high-contrast outline border.
 */
@Composable
fun CapsuleToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

@Preview
@Composable
private fun CapsuleToolbarPreview() {
    DualThemePreview {
        CapsuleToolbar {
            ToolIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {}
            )
            ToolIconButton(
                icon = Icons.Default.PlayArrow,
                contentDescription = "Play",
                onClick = {},
                isSelected = true
            )
            ToolIconButton(
                icon = Icons.Default.Undo,
                contentDescription = "Undo",
                onClick = {}
            )
            ToolIconButton(
                icon = Icons.Default.Redo,
                contentDescription = "Redo",
                onClick = {},
                isEnabled = false
            )
        }
    }
}