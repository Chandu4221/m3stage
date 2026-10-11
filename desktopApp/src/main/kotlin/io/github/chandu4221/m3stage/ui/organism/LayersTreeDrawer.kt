package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.ui.molecule.LayerTreeRow
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Organism: Layers & Tree drawer panel replacing PartsDrawer when the Layers tab is active.
 * Renders the hierarchical DOM-like component tree for the currently active screen.
 * Supports expanding/collapsing nodes, node selection, locking, visibility, and deletion.
 */
@Composable
fun LayersTreeDrawer(
    activeScreen: Screen?,
    selectedNodeId: NodeId?,
    lockedNodeIds: Set<NodeId>,
    onSelectNode: (NodeId) -> Unit,
    onToggleVisibility: (NodeId) -> Unit,
    onToggleLock: (NodeId) -> Unit,
    onDeleteNode: (NodeId) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedNodeIds by remember { mutableStateOf(setOf<NodeId>()) }

    // Automatically ensure the root is expanded initially
    LaunchedEffect(activeScreen?.root?.id) {
        activeScreen?.root?.id?.let { rootId ->
            if (rootId !in expandedNodeIds) {
                expandedNodeIds = expandedNodeIds + rootId
            }
        }
    }

    Surface(
        modifier = modifier.width(280.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Title & Screen Context
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Layers & Tree",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (activeScreen != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${activeScreen.name} (${activeScreen.route})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            if (activeScreen == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "No active screen selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    renderTreeItem(
                        node = activeScreen.root,
                        depth = 0,
                        slotBadge = null,
                        selectedNodeId = selectedNodeId,
                        lockedNodeIds = lockedNodeIds,
                        expandedNodeIds = expandedNodeIds,
                        onToggleExpand = { id ->
                            expandedNodeIds = if (id in expandedNodeIds) {
                                expandedNodeIds - id
                            } else {
                                expandedNodeIds + id
                            }
                        },
                        onSelectNode = onSelectNode,
                        onToggleVisibility = onToggleVisibility,
                        onToggleLock = onToggleLock,
                        onDeleteNode = onDeleteNode
                    )
                }
            }
        }
    }
}

/**
 * Recursive tree flattener into LazyColumn items.
 */
private fun androidx.compose.foundation.lazy.LazyListScope.renderTreeItem(
    node: DesignNode,
    depth: Int,
    slotBadge: String?,
    selectedNodeId: NodeId?,
    lockedNodeIds: Set<NodeId>,
    expandedNodeIds: Set<NodeId>,
    onToggleExpand: (NodeId) -> Unit,
    onSelectNode: (NodeId) -> Unit,
    onToggleVisibility: (NodeId) -> Unit,
    onToggleLock: (NodeId) -> Unit,
    onDeleteNode: (NodeId) -> Unit
) {
    val isExpanded = node.id in expandedNodeIds
    val hasChildren = node.allDirectChildren.isNotEmpty()

    item(key = node.id.value) {
        LayerTreeRow(
            node = node,
            depth = depth,
            isSelected = node.id == selectedNodeId,
            isLocked = node.id in lockedNodeIds,
            isExpanded = isExpanded,
            hasChildren = hasChildren,
            slotBadge = slotBadge,
            onSelect = { onSelectNode(node.id) },
            onToggleExpand = { onToggleExpand(node.id) },
            onToggleVisibility = { onToggleVisibility(node.id) },
            onToggleLock = { onToggleLock(node.id) },
            onDelete = { onDeleteNode(node.id) }
        )
    }

    if (isExpanded) {
        // 1. Render named slots children first
        val slotChildIds = mutableSetOf<NodeId>()
        for ((slotId, slotChildren) in node.slots) {
            for (child in slotChildren) {
                slotChildIds.add(child.id)
                renderTreeItem(
                    node = child,
                    depth = depth + 1,
                    slotBadge = slotId.value,
                    selectedNodeId = selectedNodeId,
                    lockedNodeIds = lockedNodeIds,
                    expandedNodeIds = expandedNodeIds,
                    onToggleExpand = onToggleExpand,
                    onSelectNode = onSelectNode,
                    onToggleVisibility = onToggleVisibility,
                    onToggleLock = onToggleLock,
                    onDeleteNode = onDeleteNode
                )
            }
        }

        // 2. Render general raw children (excluding any already rendered in slots)
        for (child in node.children) {
            if (child.id !in slotChildIds) {
                renderTreeItem(
                    node = child,
                    depth = depth + 1,
                    slotBadge = null,
                    selectedNodeId = selectedNodeId,
                    lockedNodeIds = lockedNodeIds,
                    expandedNodeIds = expandedNodeIds,
                    onToggleExpand = onToggleExpand,
                    onSelectNode = onSelectNode,
                    onToggleVisibility = onToggleVisibility,
                    onToggleLock = onToggleLock,
                    onDeleteNode = onDeleteNode
                )
            }
        }
    }
}

@Preview
@Composable
private fun LayersTreeDrawerPreview() {
    DualThemePreview {
        Box(modifier = Modifier.height(480.dp)) {
            LayersTreeDrawer(
                activeScreen = null,
                selectedNodeId = null,
                lockedNodeIds = emptySet(),
                onSelectNode = {},
                onToggleVisibility = {},
                onToggleLock = {},
                onDeleteNode = {}
            )
        }
    }
}
