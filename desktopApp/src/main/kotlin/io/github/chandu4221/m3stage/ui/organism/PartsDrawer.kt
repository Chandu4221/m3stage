package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.ui.atom.SearchTextField
import io.github.chandu4221.m3stage.ui.molecule.ComponentTile
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

@Composable
fun PartsDrawer(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onComponentSelected: (ComponentKind) -> Unit,
    modifier: Modifier = Modifier,
    activeCategory: ComponentCategory? = null,
    onCategorySelected: (ComponentCategory?) -> Unit = {},
    disallowedKinds: Set<ComponentKind> = emptySet(),
    onComponentDragStart: (ComponentKind) -> Unit = {},
    onComponentDragEnd: () -> Unit = {}
) {
    Surface(
        modifier = modifier.width(280.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Title & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Components",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                SearchTextField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholderText = "Filter parts...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Category Filter Row
            ScrollableTabRow(
                selectedTabIndex = if (activeCategory == null) 0 else ComponentCategory.entries.indexOf(activeCategory) + 1,
                edgePadding = 12.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeCategory == null,
                    onClick = { onCategorySelected(null) },
                    text = { Text("All") }
                )
                ComponentCategory.entries.forEach { category ->
                    Tab(
                        selected = activeCategory == category,
                        onClick = { onCategorySelected(category) },
                        text = { Text(category.displayName) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Filtered Items List
            val allComponents: List<ComponentDefinition> = remember { ComponentCatalog.all.filter { it.showInPalette } }
            val filteredComponents = remember(searchQuery, activeCategory) {
                allComponents.filter { def ->
                    val matchesSearch = searchQuery.isBlank() ||
                        def.displayName.contains(searchQuery, ignoreCase = true) ||
                        def.category.displayName.contains(searchQuery, ignoreCase = true)
                    val matchesCategory = activeCategory == null || def.category == activeCategory
                    matchesSearch && matchesCategory
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grouped = filteredComponents.groupBy { it.category }
                grouped.forEach { (category, defs) ->
                    item {
                        Text(
                            text = category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // Render grid pairs per row
                    val chunked = defs.chunked(2)
                    items(chunked) { rowDefs ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowDefs.forEach { def ->
                                ComponentTile(
                                    displayName = def.displayName,
                                    icon = resolveComponentIcon(def.kind),
                                    onClick = { onComponentSelected(def.kind) },
                                    enabled = def.kind !in disallowedKinds,
                                    onDragStart = { onComponentDragStart(def.kind) },
                                    onDragEnd = { onComponentDragEnd() },
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
}

private fun resolveComponentIcon(kind: ComponentKind): ImageVector = when (kind) {
    // Communication
    ComponentKind.Text -> Icons.Default.TextFields
    ComponentKind.Icon -> Icons.Default.Star
    ComponentKind.Image -> Icons.Default.Image
    ComponentKind.Badge -> Icons.Default.Notifications

    // Actions
    ComponentKind.Button -> Icons.Default.SmartButton
    ComponentKind.IconButton -> Icons.Default.TouchApp
    ComponentKind.FloatingActionButton -> Icons.Default.AddCircle
    ComponentKind.OutlinedButton -> Icons.Default.CropPortrait
    ComponentKind.TextButton -> Icons.Default.Subject

    // Text Inputs
    ComponentKind.TextField -> Icons.Default.EditNote
    ComponentKind.OutlinedTextField -> Icons.Default.Edit

    // Layouts
    ComponentKind.Column -> Icons.Default.ViewColumn
    ComponentKind.Row -> Icons.Default.TableRows
    ComponentKind.Box -> Icons.Default.CheckBoxOutlineBlank
    ComponentKind.Spacer -> Icons.Default.SpaceBar

    // Containment
    ComponentKind.Scaffold -> Icons.Default.Web
    ComponentKind.Card -> Icons.Default.CropSquare
    ComponentKind.HorizontalDivider -> Icons.Default.HorizontalRule

    // Navigation
    ComponentKind.TopAppBar -> Icons.AutoMirrored.Filled.MenuOpen
    ComponentKind.NavigationBar -> Icons.Default.Navigation
    ComponentKind.NavigationBarItem -> Icons.Default.Navigation

    // Selection
    ComponentKind.Switch -> Icons.Default.ToggleOn
    ComponentKind.Checkbox -> Icons.Default.CheckBox
    ComponentKind.RadioButton -> Icons.Default.RadioButtonChecked
    ComponentKind.Slider -> Icons.Default.LinearScale
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
