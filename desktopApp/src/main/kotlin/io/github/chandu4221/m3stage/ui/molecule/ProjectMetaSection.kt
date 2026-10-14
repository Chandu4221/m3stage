package io.github.chandu4221.m3stage.ui.molecule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.ui.preview.DualThemePreview

/**
 * Molecule: Collapsible Project Metadata section for the drawer header.
 * Displays project name and base package in a compact card, with an expandable
 * inline editor with real-time validation against Kotlin/Java package naming rules.
 */
@Composable
fun ProjectMetaSection(
    projectName: String,
    basePackage: String,
    onUpdateProjectMetadata: (name: String, basePackage: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }

    var draftName by remember(projectName, isEditing) { mutableStateOf(projectName) }
    var draftPackage by remember(basePackage, isEditing) { mutableStateOf(basePackage) }

    val isNameValid = Project.isValidProjectName(draftName)
    val isPackageValid = Project.isValidPackageName(draftPackage)
    val hasChanges = draftName.trim() != projectName.trim() || draftPackage.trim() != basePackage.trim()
    val canSave = isNameValid && isPackageValid && hasChanges

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Header summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isEditing = !isEditing },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = projectName.ifBlank { "Untitled Project" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = basePackage.ifBlank { "com.example.app" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.ExpandLess else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Collapse" else "Edit Project Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expandable editor form
            AnimatedVisibility(visible = isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Project Name Input
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        isError = !isNameValid,
                        supportingText = {
                            if (!isNameValid) {
                                Text("Project name cannot be blank", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Base Package Input
                    OutlinedTextField(
                        value = draftPackage,
                        onValueChange = { draftPackage = it },
                        label = { Text("Base Package") },
                        singleLine = true,
                        isError = !isPackageValid,
                        supportingText = {
                            if (!isPackageValid) {
                                Text(
                                    "Must follow package rules (e.g. com.example.app)",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Form action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                draftName = projectName
                                draftPackage = basePackage
                                isEditing = false
                            }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (canSave) {
                                    onUpdateProjectMetadata(draftName.trim(), draftPackage.trim())
                                    isEditing = false
                                }
                            },
                            enabled = canSave
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProjectMetaSectionPreview() {
    DualThemePreview {
        Box(modifier = Modifier.width(280.dp).padding(8.dp)) {
            ProjectMetaSection(
                projectName = "MyComposeApp",
                basePackage = "com.example.app",
                onUpdateProjectMetadata = { _, _ -> }
            )
        }
    }
}
