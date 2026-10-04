package io.github.chandu4221.m3stage.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findScreen
import io.github.chandu4221.m3stage.state.EditorStore

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
            .fillMaxHeight()
            .width(250.dp)
            .padding(16.dp)
    ) {
        Text("Inspector", style = MaterialTheme.typography.titleMedium)

        if (selectedNodeId == null || project == null || activeScreenId == null) {
            Text(
                text = "Select a node to edit its properties.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
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
    Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Type: ${node.type.value}", style = MaterialTheme.typography.titleSmall)
            Text(
                "ID: ${node.id.value}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))

            // Visibility Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Visible", modifier = Modifier.weight(1f))
                Switch(
                    checked = node.isVisible,
                    onCheckedChange = { store.toggleVisibility(node.id) }
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

            // Lock Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Locked", modifier = Modifier.weight(1f))
                Switch(
                    checked = isLocked,
                    onCheckedChange = { store.toggleLock(node.id) }
                )
            }
        }
    }
}