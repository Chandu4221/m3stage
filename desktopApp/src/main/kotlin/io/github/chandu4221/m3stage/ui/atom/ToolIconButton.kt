package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Atom: Accessible 48x48dp icon button for toolbars and rails.
 * Observes active/selected states, disables cleanly, and displays an optional hover tooltip.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolIconButton(
    icon: ImageVector,
    contentDescription: String = "",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    tooltip: String? = null
) {
    val buttonContent = @Composable {
        IconButton(
            onClick = onClick,
            enabled = isEnabled,
            modifier = modifier
                .size(44.dp)
                .semantics { role = Role.Button },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (tooltip != null) {
        TooltipArea(
            delayMillis = 500, // 500ms delay so it doesn't pop up instantly over mouse
            tooltip = {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = tooltip,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            },
            // Places the tooltip cleanly ABOVE the button with an 8dp gap
            tooltipPlacement = TooltipPlacement.ComponentRect(
                anchor = Alignment.TopCenter,
                alignment = Alignment.TopCenter,
                offset = DpOffset(x = 0.dp, y = (-8).dp)
            )
        ) {
            buttonContent()
        }
    } else {
        buttonContent()
    }
}

@Preview
@Composable
private fun ToolIconButtonPreview() {
    DualThemePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToolIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {}
            )
            ToolIconButton(
                icon = Icons.Default.PlayArrow,
                contentDescription = "Selected",
                isSelected = true,
                onClick = {}
            )
        }
    }
}