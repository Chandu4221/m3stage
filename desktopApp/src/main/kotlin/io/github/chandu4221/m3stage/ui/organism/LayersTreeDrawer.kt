package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
data class LayerRowItem(
    val id: NodeId,
    val depth: Int,
    val node: DesignNode,
    val slotBadge: String?,
    val parentId: NodeId? = null
)

@Composable
fun LayersTreeDrawer(
    activeScreen: Screen?,
    selectedNodeId: NodeId?,
    lockedNodeIds: Set<NodeId>,
    onSelectNode: (NodeId) -> Unit,
    onToggleVisibility: (NodeId) -> Unit,
    onToggleLock: (NodeId) -> Unit,
    onDeleteNode: (NodeId) -> Unit,
    selectedNodeIds: Set<NodeId> = selectedNodeId?.let { setOf(it) } ?: emptySet(),
    modifier: Modifier = Modifier.width(280.dp)
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

    // T4.5: Memoize flattened row model per activeScreen (immutable tree)
    val allRows = remember(activeScreen) {
        if (activeScreen == null) emptyList() else flattenTree(activeScreen.root)
    }

    // T4.5: Derive visible rows from expandedNodeIds and memoized allRows
    val visibleRows = remember(expandedNodeIds, allRows) {
        if (allRows.isEmpty()) emptyList() else filterVisibleRows(allRows, expandedNodeIds)
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
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
                    items(
                        items = visibleRows,
                        key = { it.id.value }
                    ) { row ->
                        val isExpanded = row.id in expandedNodeIds
                        val hasChildren = row.node.allDirectChildren.isNotEmpty()

                        LayerTreeRow(
                            node = row.node,
                            depth = row.depth,
                            isSelected = if (selectedNodeIds.isNotEmpty()) row.id in selectedNodeIds else row.id == selectedNodeId,
                            isLocked = row.id in lockedNodeIds,
                            isExpanded = isExpanded,
                            hasChildren = hasChildren,
                            slotBadge = row.slotBadge,
                            onSelect = { onSelectNode(row.id) },
                            onToggleExpand = {
                                expandedNodeIds = if (row.id in expandedNodeIds) {
                                    expandedNodeIds - row.id
                                } else {
                                    expandedNodeIds + row.id
                                }
                            },
                            onToggleVisibility = { onToggleVisibility(row.id) },
                            onToggleLock = { onToggleLock(row.id) },
                            onDelete = { onDeleteNode(row.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Flattens the tree into a list of LayerRowItems capturing parent relationships for expansion filtering.
 */
private fun flattenTree(
    root: DesignNode,
    depth: Int = 0,
    slotBadge: String? = null,
    parentId: NodeId? = null
): List<LayerRowItem> {
    val result = mutableListOf<LayerRowItem>()
    result.add(LayerRowItem(root.id, depth, root, slotBadge, parentId))

    // 1. Named slots first
    val slotChildIds = mutableSetOf<NodeId>()
    for ((slotId, slotChildren) in root.slots) {
        for (child in slotChildren) {
            slotChildIds.add(child.id)
            result.addAll(flattenTree(child, depth + 1, slotId.value, root.id))
        }
    }

    // 2. General raw children (excluding slot children)
    for (child in root.children) {
        if (child.id !in slotChildIds) {
            result.addAll(flattenTree(child, depth + 1, null, root.id))
        }
    }

    return result
}

/**
 * Filters rows to only include those whose ancestors are all expanded.
 */
private fun filterVisibleRows(
    rows: List<LayerRowItem>,
    expandedNodeIds: Set<NodeId>
): List<LayerRowItem> {
    val visible = mutableListOf<LayerRowItem>()
    val visibleParentIds = mutableSetOf<NodeId>()

    for (row in rows) {
        val parentId = row.parentId
        if (parentId == null || (parentId in expandedNodeIds && parentId in visibleParentIds)) {
            visible.add(row)
            visibleParentIds.add(row.id)
        }
    }

    return visible
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
