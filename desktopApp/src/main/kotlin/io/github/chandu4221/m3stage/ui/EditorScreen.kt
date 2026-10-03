package io.github.chandu4221.m3stage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.model.ComponentTypes
import io.github.chandu4221.m3stage.state.EditorStore

@Composable
fun EditorScreen(store: EditorStore) {
    val project by store.project.collectAsState()
    val activeScreenId by store.activeScreenId
    val selectedNodeId by store.selectedNodeId // <-- Track actual selection

    Row(modifier = Modifier.fillMaxSize()) {
        // Left: Palette
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp)
        ) {
            Text("Palette", modifier = Modifier.width(200.dp))

            Button(
                onClick = {
                    project?.screens?.firstOrNull { it.id == activeScreenId }?.let { screen ->
                        store.addNodeToActiveScreen(screen.root.id, ComponentTypes.Text)
                    }
                },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Add Text")
            }

            Button(
                onClick = {
                    project?.screens?.firstOrNull { it.id == activeScreenId }?.let { screen ->
                        store.addNodeToActiveScreen(screen.root.id, ComponentTypes.Button)
                    }
                },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Add Button")
            }

            Button(
                onClick = { store.saveProject() },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Save")
            }

            Button(
                onClick = { store.undo() },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Undo")
            }
        }

        // Center: Canvas
        Column(modifier = Modifier.fillMaxSize().weight(1f)) {
            Text("Canvas")

            project?.screens?.firstOrNull { it.id == activeScreenId }?.let { screen ->
                DesignRenderer.Render(
                    node = screen.root,
                    selectedNodeId = selectedNodeId, // <-- Use actual state
                    onNodeClick = { nodeId -> store.selectNode(nodeId) } // <-- Wire up click
                )
            } ?: Text("No active screen")
        }
    }
}