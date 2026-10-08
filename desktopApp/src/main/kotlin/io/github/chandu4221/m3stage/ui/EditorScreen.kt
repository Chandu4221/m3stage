package io.github.chandu4221.m3stage.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.chandu4221.m3stage.model.DevicePreset
import io.github.chandu4221.m3stage.state.EditorStore

@Composable
fun EditorScreen(store: EditorStore) {
    val project by store.project.collectAsState()
    val activeScreenId by store.activeScreenId
    val selectedNodeId by store.selectedNodeId
    val lockedNodeIds by store.lockedNodeIds

    Column(modifier = Modifier.fillMaxSize()) {
// === TOP 2-TIER PROJECT APP BAR ===
        TopProjectAppBar(
            project = project,
            activeScreenId = activeScreenId,
            store = store
        )


        Row(modifier = Modifier.fillMaxSize()) {

            // === COMPONENTS PALETTE ===
            ComponentsPalettePanel(store = store)

            // Center: Canvas
            Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                Text("Canvas")

                project?.screens?.firstOrNull { it.id == activeScreenId }?.let { screen ->
                    CanvasPanel(
                        screen = screen,
                        selectedNodeId = selectedNodeId,
                        onNodeClick = { nodeId -> store.selectNode(nodeId) },
                        lockedNodeIds = lockedNodeIds,
                        projectDefaultDevice = project?.defaultDevice ?: DevicePreset.Default
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active screen. Create a new project.")
                }
            }

            // ==== INSPECTOR COLUMN
            // Inside the Row in EditorScreen.kt, add this as the 3rd child:
            InspectorPanel(
                project = project,
                activeScreenId = activeScreenId?.value,
                selectedNodeId = selectedNodeId,
                isLocked = selectedNodeId?.let { store.isNodeLocked(it) } ?: false,
                store = store
            )
        }
    }
}