package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import io.github.chandu4221.m3stage.model.DesignNode

/**
 * M3 Context Menu displayed on right-click in the device canvas.
 * Adapts options based on whether target is a component node or empty artboard space.
 */
@Composable
fun NodeContextMenu(
    expanded: Boolean,
    offset: DpOffset,
    targetNode: DesignNode?,
    isLocked: Boolean,
    canPaste: Boolean,
    hasMultipleSelection: Boolean = false,
    onDismissRequest: () -> Unit,
    onRename: () -> Unit = {},
    onCopy: () -> Unit = {},
    onCut: () -> Unit = {},
    onPaste: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleLock: () -> Unit = {},
    onToggleVisibility: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeselect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        modifier = modifier
    ) {
        if (targetNode != null) {
            DropdownMenuItem(
                text = { Text("Rename") },
                onClick = {
                    onDismissRequest()
                    onRename()
                }
            )
            DropdownMenuItem(
                text = { Text("Copy") },
                trailingIcon = {
                    Text(
                        text = "Ctrl+C",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onCopy()
                }
            )
            DropdownMenuItem(
                text = { Text("Cut") },
                trailingIcon = {
                    Text(
                        text = "Ctrl+X",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onCut()
                }
            )
            DropdownMenuItem(
                text = { Text("Paste") },
                enabled = canPaste,
                trailingIcon = {
                    Text(
                        text = "Ctrl+V",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onPaste()
                }
            )
            DropdownMenuItem(
                text = { Text(if (hasMultipleSelection) "Duplicate Selected" else "Duplicate") },
                trailingIcon = {
                    Text(
                        text = "Ctrl+D",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onDuplicate()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (hasMultipleSelection) "Delete Selected" else "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                trailingIcon = {
                    Text(
                        text = "Del",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    onDismissRequest()
                    onDelete()
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(if (isLocked) "Unlock" else "Lock") },
                onClick = {
                    onDismissRequest()
                    onToggleLock()
                }
            )
            DropdownMenuItem(
                text = { Text(if (targetNode.isVisible) "Hide" else "Show") },
                onClick = {
                    onDismissRequest()
                    onToggleVisibility()
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Paste") },
                enabled = canPaste,
                trailingIcon = {
                    Text(
                        text = "Ctrl+V",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onPaste()
                }
            )
            DropdownMenuItem(
                text = { Text("Select All") },
                trailingIcon = {
                    Text(
                        text = "Ctrl+A",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onSelectAll()
                }
            )
            DropdownMenuItem(
                text = { Text("Deselect") },
                trailingIcon = {
                    Text(
                        text = "Esc",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onDismissRequest()
                    onDeselect()
                }
            )
        }
    }
}
