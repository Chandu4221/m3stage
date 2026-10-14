package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.awt.Cursor

/**
 * Draggable vertical splitter divider separating studio panels from the canvas.
 * - Supports horizontal dragging to resize adjacent panels.
 * - Double-click resets the panel width to its default size.
 * - Displays horizontal resize cursor on hover (⟷).
 * - Highlights with primary color on hover or active drag.
 */
@Composable
fun PanelSplitter(
    onDragDelta: (Float) -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isDragging by remember { mutableStateOf(false) }

    val resizeCursor = remember {
        PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR))
    }

    Box(
        modifier = modifier
            .width(6.dp)
            .fillMaxHeight()
            .pointerHoverIcon(resizeCursor)
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleClick() }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { _, dragAmount ->
                        onDragDelta(dragAmount)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual indicator line
        Box(
            modifier = Modifier
                .width(if (isHovered || isDragging) 2.dp else 1.dp)
                .fillMaxHeight()
                .background(
                    if (isHovered || isDragging) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                )
        )
    }
}
