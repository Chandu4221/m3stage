package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.ui.atom.SearchTextField
import io.github.chandu4221.m3stage.ui.molecule.ComponentTile
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Dumb Organism: 2-column "Parts" component catalog drawer.
 * Accepts search query and emitted callbacks, zero store awareness.
 */
@Composable
fun PartsDrawer(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onComponentSelected: (ComponentKind) -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedCategories = remember {
        mutableStateMapOf<ComponentCategory, Boolean>().apply {
            ComponentCategory.entries.forEach { put(it, true) }
        }
    }

    Surface(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Text(
                text = "Parts",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            // Search Bar
            SearchTextField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholderText = "Search parts..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable 2-Column Categories
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComponentCatalog.groupedByCategory.forEach { (category, definitions) ->
                    val filteredDefs = if (searchQuery.isBlank()) definitions else {
                        definitions.filter { it.displayName.contains(searchQuery, ignoreCase = true) }
                    }

                    if (filteredDefs.isNotEmpty()) {
                        PartsCategorySection(
                            category = category,
                            isExpanded = expandedCategories[category] ?: true,
                            onToggle = { expandedCategories[category] = !(expandedCategories[category] ?: true) },
                            definitions = filteredDefs,
                            onComponentSelected = onComponentSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartsCategorySection(
    category: ComponentCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    definitions: List<ComponentDefinition>,
    onComponentSelected: (ComponentKind) -> Unit
) {
    Column {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { role = Role.Button }
                .clickable(onClick = onToggle)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // 2-Column Grid Tiles
        AnimatedVisibility(visible = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                definitions.chunked(2).forEach { rowDefs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowDefs.forEach { def ->
                            ComponentTile(
                                displayName = def.displayName,
                                icon = resolveComponentIcon(def.kind),
                                onClick = { onComponentSelected(def.kind) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowDefs.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun resolveComponentIcon(kind: ComponentKind): ImageVector = when (kind) {
    ComponentKind.Text -> Icons.Default.TextFields
    ComponentKind.Button -> Icons.Default.SmartButton
    ComponentKind.Column -> Icons.Default.ViewColumn
    ComponentKind.Row -> Icons.Default.TableRows
    ComponentKind.Box -> Icons.Default.CheckBoxOutlineBlank
    ComponentKind.Card -> Icons.Default.CropSquare
    ComponentKind.Icon -> Icons.Default.Image
    ComponentKind.Image -> Icons.Default.Image
    ComponentKind.TextField -> Icons.AutoMirrored.Filled.Input
    ComponentKind.Scaffold -> Icons.Default.Portrait
    ComponentKind.TopAppBar -> Icons.Default.WebAsset
}

@Preview
@Composable
private fun PartsDrawerPreview() {
    DualThemePreview {
        var query by remember { mutableStateOf("") }
        Box(modifier = Modifier.height(480.dp)) {
            PartsDrawer(
                searchQuery = query,
                onSearchQueryChange = { query = it },
                onComponentSelected = {}
            )
        }
    }
}