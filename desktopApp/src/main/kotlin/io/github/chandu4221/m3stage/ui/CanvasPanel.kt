package io.github.chandu4221.m3stage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.adapter.renderer.DesignRenderer
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Screen

@Composable
fun CanvasPanel(
    screen: Screen,
    selectedNodeId: NodeId?,
    onNodeClick: (NodeId) -> Unit,
    lockedNodeIds: Set<NodeId> = emptySet()
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Surface(
            modifier = Modifier.padding(8.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "Screen: ${screen.name}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Scrollable Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F0F0)) // Light gray background
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // The "Artboard" (Device Frame)
            Surface(
                modifier = Modifier
                    .width(400.dp) // Fixed width for MVP (can be dynamic later)
                    .shadow(8.dp, MaterialTheme.shapes.medium),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    // Render the actual design tree
                    DesignRenderer.Render(
                        node = screen.root,
                        selectedNodeId = selectedNodeId,
                        onNodeClick = onNodeClick,
                        lockedNodeIds = lockedNodeIds
                    )
                }
            }
        }
    }
}