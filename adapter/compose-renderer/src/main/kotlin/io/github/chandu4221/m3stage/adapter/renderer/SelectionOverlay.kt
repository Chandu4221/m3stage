package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.background
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
    val (borderWidth, borderColor, backgroundColor) = when {
        isLocked -> Triple(2.dp, MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f))
        isSelected -> Triple(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        isHovered -> Triple(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f), MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        else -> Triple(0.dp, Color.Transparent, Color.Transparent)
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
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        content()
    }
}