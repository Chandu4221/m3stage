package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Molecule: Single row in the Layers & Tree panel.
 * Represents a single component node with indentation, expand/collapse caret,
 * icon, component title, slot badge, and inline quick actions (visibility, lock, delete).
 */
@Composable
fun LayerTreeRow(
    node: DesignNode,
    depth: Int,
    isSelected: Boolean,
    isLocked: Boolean,
    isExpanded: Boolean,
    hasChildren: Boolean,
    slotBadge: String?,
    onSelect: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indentPadding = (depth * 16).dp
    val surfaceColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val border = if (isSelected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .semantics { role = Role.Button }
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(8.dp),
        color = surfaceColor,
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = indentPadding + 4.dp, end = 4.dp)
                .alpha(if (node.isVisible) 1f else 0.45f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Expand / Collapse Caret
            if (hasChildren) {
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }

            Spacer(modifier = Modifier.width(2.dp))

            // 2. Component Icon
            Icon(
                imageVector = resolveTreeIcon(node.kind),
                contentDescription = node.kind.displayName,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // 3. Component Title & Secondary Subtitle
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.kind.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional slot badge (e.g. "topBar", "FAB", "content")
                if (!slotBadge.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = slotBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // 4. Quick Actions (Visibility, Lock, Delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Visibility Toggle
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (node.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (node.isVisible) "Hide element" else "Show element",
                        tint = if (node.isVisible) contentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Lock Toggle
                IconButton(
                    onClick = onToggleLock,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isLocked) "Unlock element" else "Lock element",
                        tint = if (isLocked) MaterialTheme.colorScheme.tertiary else contentColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete element",
                        tint = contentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Resolves standard Material 3 icons for tree view items.
 */
fun resolveTreeIcon(kind: ComponentKind): ImageVector = when (kind) {
    ComponentKind.Text -> Icons.Default.TextFields
    ComponentKind.Icon -> Icons.Default.Star
    ComponentKind.Image -> Icons.Default.Image
    ComponentKind.Badge -> Icons.Default.Notifications
    ComponentKind.Button -> Icons.Default.SmartButton
    ComponentKind.IconButton -> Icons.Default.TouchApp
    ComponentKind.FloatingActionButton -> Icons.Default.AddCircle
    ComponentKind.OutlinedButton -> Icons.Default.CropPortrait
    ComponentKind.TextButton -> Icons.Default.Subject
    ComponentKind.TextField -> Icons.Default.EditNote
    ComponentKind.OutlinedTextField -> Icons.Default.Edit
    ComponentKind.Column -> Icons.Default.ViewColumn
    ComponentKind.Row -> Icons.Default.TableRows
    ComponentKind.Box -> Icons.Default.CheckBoxOutlineBlank
    ComponentKind.Spacer -> Icons.Default.SpaceBar
    ComponentKind.Scaffold -> Icons.Default.Web
    ComponentKind.Card -> Icons.Default.CropSquare
    ComponentKind.HorizontalDivider -> Icons.Default.HorizontalRule
    ComponentKind.TopAppBar -> Icons.AutoMirrored.Filled.MenuOpen
    ComponentKind.NavigationBar -> Icons.Default.Navigation
    ComponentKind.NavigationBarItem -> Icons.Default.Navigation
    ComponentKind.Switch -> Icons.Default.ToggleOn
    ComponentKind.Checkbox -> Icons.Default.CheckBox
    ComponentKind.RadioButton -> Icons.Default.RadioButtonChecked
    ComponentKind.Slider -> Icons.Default.LinearScale
}

@Preview
@Composable
private fun LayerTreeRowPreview() {
    DualThemePreview {
        Box(modifier = Modifier.padding(16.dp)) {
            LayerTreeRow(
                node = DesignNode(
                    id = NodeId("sample_button"),
                    kind = ComponentKind.Button
                ),
                depth = 1,
                isSelected = true,
                isLocked = false,
                isExpanded = true,
                hasChildren = true,
                slotBadge = "FAB",
                onSelect = {},
                onToggleExpand = {},
                onToggleVisibility = {},
                onToggleLock = {},
                onDelete = {}
            )
        }
    }
}
