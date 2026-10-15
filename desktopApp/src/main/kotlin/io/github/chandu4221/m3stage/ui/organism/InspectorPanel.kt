package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findScreen
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.ui.molecule.ModifierSection
import io.github.chandu4221.m3stage.ui.molecule.SegmentedButtonGroup

@Composable
fun InspectorPanel(
    project: Project?,
    activeScreenId: String?,
    selectedNodeId: NodeId?,
    isLocked: Boolean,
    store: EditorStore
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Inspector",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (project == null || activeScreenId == null) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No Selection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Click any composable in the canvas or component hierarchy to inspect and edit its properties.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        if (selectedNodeId == null) {
            val activeScreen = project.findScreen(io.github.chandu4221.m3stage.model.ScreenId(activeScreenId))
            if (activeScreen != null) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Screen Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Adaptive Layout",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Adapt UI components to window size class breakpoints",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = activeScreen.adaptive,
                                onCheckedChange = { isChecked ->
                                    store.setScreenAdaptive(activeScreen.id, isChecked)
                                }
                            )
                        }
                    }
                }
            }
            return
        }

        val selectedIds = store.selectedNodeIds.value
        if (selectedIds.size > 1) {
            val screen = project.findScreen(io.github.chandu4221.m3stage.model.ScreenId(activeScreenId))
            val selectedNodes = selectedIds.mapNotNull { screen?.root?.findNode(it) }
            val hasPrefilled = selectedNodes.any { it.isPrefilled }

            if (hasPrefilled) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Placeholder",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${selectedIds.size} items selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Batch actions apply to all selected items.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { store.duplicateSelectedNodes() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Duplicate")
                        }
                        Button(
                            onClick = { store.removeSelectedNodes() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedIds.forEach { store.toggleLock(it) }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Toggle Lock")
                        }
                        OutlinedButton(
                            onClick = {
                                selectedNodes.forEach { node ->
                                    store.toggleVisibility(node.id)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Toggle Hide")
                        }
                    }
                }
            }
            return
        }

        val screen = project.findScreen(io.github.chandu4221.m3stage.model.ScreenId(activeScreenId))
        val node = screen?.root?.findNode(selectedNodeId)

        if (node != null) {
            NodePropertiesCard(node, isLocked, store)
        }
    }
}

@Composable
private fun NodePropertiesCard(node: DesignNode, isLocked: Boolean, store: EditorStore) {
    if (node.isPrefilled) {
        AssistChip(
            onClick = {},
            label = {
                Text(
                    text = "Placeholder",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = null,
            modifier = Modifier.padding(top = 12.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (node.isPrefilled) 8.dp else 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = node.label ?: node.kind.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (node.label != null) {
                Text(
                    text = "Type: ${node.kind.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "ID: ${node.id.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    val def = ComponentCatalog.find(node.kind)
    val variantSpec = def?.variantSpec

    if (variantSpec != null) {
        val currentVariantName = variantSpec.resolveVariantName(node)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = variantSpec.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SegmentedButtonGroup(
                    options = variantSpec.optionNames,
                    selectedOption = currentVariantName,
                    onOptionSelected = { selectedName ->
                        if (!isLocked) {
                            store.updateProp(
                                node.id,
                                variantSpec.key.id,
                                PropertyValue.EnumValue(selectedName)
                            )
                        }
                    },
                    labelProvider = { it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    val visibleProperties = def?.properties?.filter { prop ->
        prop.key.id != variantSpec?.key?.id && (def.isPropVisible(node, prop.key.id))
    }.orEmpty()

    val textProps = visibleProperties.filterIsInstance<PropertySpec.Text>()
    val enumProps = visibleProperties.filterIsInstance<PropertySpec.EnumDropdown>()
    val colorProps = visibleProperties.filterIsInstance<PropertySpec.ColorPicker>()
    val dpProps = visibleProperties.filterIsInstance<PropertySpec.DpSlider>()

    if (textProps.isNotEmpty() || enumProps.isNotEmpty() || colorProps.isNotEmpty() || dpProps.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Properties",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                for (prop in textProps) {
                    val currentVal = (node.props[prop.key.id] as? PropertyValue.StringValue)?.value
                        ?: prop.defaultValue.value
                    var textState by remember(node.id, prop.key.id, currentVal) { mutableStateOf(currentVal) }

                    OutlinedTextField(
                        value = textState,
                        onValueChange = { newText ->
                            textState = newText
                            store.updateProp(node.id, prop.key.id, PropertyValue.StringValue(newText))
                        },
                        label = { Text(prop.displayName) },
                        enabled = !isLocked,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                for (prop in enumProps) {
                    val currentVal = (node.props[prop.key.id] as? PropertyValue.EnumValue)?.name
                        ?: prop.defaultValue.name
                    var expanded by remember(node.id, prop.key.id) { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentVal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(prop.displayName) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { if (!isLocked) expanded = !expanded },
                                    enabled = !isLocked
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand ${prop.displayName}"
                                    )
                                }
                            },
                            enabled = !isLocked,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            for (option in prop.options) {
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        expanded = false
                                        store.updateProp(node.id, prop.key.id, PropertyValue.EnumValue(option))
                                        if (node.kind == io.github.chandu4221.m3stage.component.ComponentKind.Icon &&
                                            prop.key.id == io.github.chandu4221.m3stage.component.definition.IconDefinition.Glyph.id
                                        ) {
                                            store.updateProp(
                                                node.id,
                                                io.github.chandu4221.m3stage.component.definition.IconDefinition.IconName.id,
                                                PropertyValue.StringValue(option)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                for (prop in colorProps) {
                    val currentVal = when (val cv = node.props[prop.key.id] ?: prop.defaultValue) {
                        is PropertyValue.ColorValue.Token -> cv.token.name
                        is PropertyValue.ColorValue.Custom -> "Custom (0x${cv.argb.toULong().toString(16).uppercase()})"
                        else -> M3ColorToken.Inherit.name
                    }
                    var expanded by remember(node.id, prop.key.id) { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (currentVal == "Inherit") "Inherit (Default)" else currentVal,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(prop.displayName) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { if (!isLocked) expanded = !expanded },
                                    enabled = !isLocked
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand ${prop.displayName}"
                                    )
                                }
                            },
                            enabled = !isLocked,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            for (token in M3ColorToken.entries) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (token == M3ColorToken.Inherit) "Inherit (Default)" else token.name)
                                    },
                                    onClick = {
                                        expanded = false
                                        store.updateProp(
                                            node.id,
                                            prop.key.id,
                                            PropertyValue.ColorValue.Token(token)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                for (prop in dpProps) {
                    val currentVal = (node.props[prop.key.id] as? PropertyValue.DpValue)?.value
                        ?: prop.defaultValue.value
                    var sliderValue by remember(node.id, prop.key.id, currentVal) { mutableStateOf(currentVal) }

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prop.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(sliderValue * 10).toInt() / 10f} dp",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = {
                                store.updateProp(node.id, prop.key.id, PropertyValue.DpValue(sliderValue))
                            },
                            valueRange = prop.min..prop.max,
                            enabled = !isLocked,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    // First-Class Modifiers Pipeline
    ModifierSection(
        node = node,
        isLocked = isLocked,
        store = store
    )
    Spacer(modifier = Modifier.height(12.dp))
}