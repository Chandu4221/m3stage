package io.github.chandu4221.m3stage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.ComponentCatalog
import io.github.chandu4221.m3stage.model.ComponentCategory
import io.github.chandu4221.m3stage.model.ComponentDefinition
import io.github.chandu4221.m3stage.state.EditorStore

@Composable
fun ComponentsPalettePanel(store: EditorStore) {
    // Track which categories are expanded. Default to true for all.
    val expandedCategories = remember {
        mutableStateMapOf<ComponentCategory, Boolean>().apply {
            ComponentCategory.entries.forEach { put(it, true) }
        }
    }

    Column(
        modifier = Modifier
            .width(240.dp)
            .padding(8.dp)
    ) {
        Text(
            text = "Components",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )

        ComponentCatalog.groupedByCategory.forEach { (category, definitions) ->
            CategorySection(
                category = category,
                isExpanded = expandedCategories[category] ?: true,
                onToggle = { expandedCategories[category] = !(expandedCategories[category] ?: true) },
                definitions = definitions,
                store = store
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategorySection(
    category: ComponentCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    definitions: List<ComponentDefinition>,
    store: EditorStore
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle $category",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Collapsible Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    definitions.forEach { definition ->
                        Button(
                            onClick = {
                                // Add to selected node if it's a container, otherwise add to screen root
                                val targetParentId = store.selectedNodeId.value
                                    ?: store.project.value?.screens?.firstOrNull { it.id == store.activeScreenId.value }?.root?.id
                                    ?: return@Button

                                store.addNodeToActiveScreen(targetParentId, definition.type)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(definition.displayName)
                        }
                    }
                }
            }
        }
    }
}