package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.NodeId

@Composable
fun SelectionOverlay(
    nodeId: NodeId,
    depth: Int,
    isSelected: Boolean,
    isLocked: Boolean,
    isHovered: Boolean,
    hitTracker: CanvasHitTracker?,
    content: @Composable () -> Unit
) {
    val (borderWidth, borderColor) = when {
        isLocked -> 2.dp to MaterialTheme.colorScheme.tertiary
        isSelected -> 2.dp to MaterialTheme.colorScheme.primary
        isHovered -> 1.5.dp to MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
        else -> 0.dp to Color.Transparent
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                hitTracker?.recordBounds(nodeId, depth, coordinates)
            }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        content()
    }
}