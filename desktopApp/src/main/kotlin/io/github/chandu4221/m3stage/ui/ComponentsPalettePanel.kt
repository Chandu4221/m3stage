package io.github.chandu4221.m3stage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.state.EditorStore

@Composable
fun ComponentsPalettePanel(store: EditorStore) {
    val expandedCategories = remember {
        mutableStateMapOf<ComponentCategory, Boolean>().apply {
            ComponentCategory.entries.forEach { put(it, true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "Components",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        ComponentCatalog.groupedByCategory.forEach { (category, definitions) ->
            CategorySection(
                category = category,
                isExpanded = expandedCategories[category] ?: true,
                onToggle = { expandedCategories[category] = !(expandedCategories[category] ?: true) },
                definitions = definitions,
                store = store
            )
            Spacer(modifier = Modifier.height(10.dp))
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column {
            // Category Header with Accessible Clickable Role
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button }
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse ${category.displayName}" else "Expand ${category.displayName}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Collapsible Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    definitions.forEach { definition ->
                        FilledTonalButton(
                            onClick = {
                                val targetParentId = store.selectedNodeId.value
                                    ?: store.project.value?.screens?.firstOrNull { it.id == store.activeScreenId.value }?.root?.id
                                    ?: return@FilledTonalButton

                                store.addNodeToActiveScreen(targetParentId, definition.kind)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = definition.displayName,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}