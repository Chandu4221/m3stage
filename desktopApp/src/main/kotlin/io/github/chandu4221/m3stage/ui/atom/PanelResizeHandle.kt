package io.github.chandu4221.m3stage.ui.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.Cursor

enum class PanelResizeSide {
    Left,
    Right,
    Bottom
}

/**
 * Code-editor-style draggable resize handle (IntelliJ / VS Code tool window splitter).
 *
 * - 8dp hit area for generous, reliable mouse target.
 * - Centered 1dp outline line; highlights to 2dp primary (40% alpha) on hover, solid primary on drag.
 * - Directional cursor (E-resize for left drawer, W-resize for right inspector, N-resize for bottom panels).
 * - Single-stream gesture handling resolving tap-vs-drag conflicts.
 * - Double-click resets adjacent panel to default width and uncollapses.
 * - Keyboard accessibility (←/→ adjusts by 8dp when focused, Ctrl+Arrow snaps to min, Home resets, End snaps to min).
 */
@Composable
fun PanelResizeHandle(
    side: PanelResizeSide,
    onDragStart: () -> Unit,
    onDragDelta: (Dp) -> Unit,
    onDragEnd: () -> Unit,
    onDoubleClick: () -> Unit,
    onAdjustWidth: (Dp) -> Unit = {},
    onSnapToMin: () -> Unit = {},
    onResetToDefault: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isDragging by remember { mutableStateOf(false) }

    val resizeCursor = remember(side) {
        val cursorId = when (side) {
            PanelResizeSide.Left -> Cursor.E_RESIZE_CURSOR
            PanelResizeSide.Right -> Cursor.W_RESIZE_CURSOR
            PanelResizeSide.Bottom -> Cursor.N_RESIZE_CURSOR
        }
        PointerIcon(Cursor.getPredefinedCursor(cursorId))
    }

    val isHorizontal = side == PanelResizeSide.Left || side == PanelResizeSide.Right

    Box(
        modifier = modifier
            .then(
                if (isHorizontal) {
                    Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                }
            )
            .pointerHoverIcon(resizeCursor)
            .hoverable(interactionSource)
            .focusable(interactionSource = interactionSource)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (side) {
                    PanelResizeSide.Left -> {
                        when {
                            keyEvent.isCtrlPressed && keyEvent.key == Key.DirectionLeft -> {
                                onSnapToMin()
                                true
                            }
                            keyEvent.key == Key.DirectionLeft -> {
                                onAdjustWidth((-8).dp)
                                true
                            }
                            keyEvent.key == Key.DirectionRight -> {
                                onAdjustWidth(8.dp)
                                true
                            }
                            keyEvent.key == Key.MoveHome -> {
                                onResetToDefault()
                                true
                            }
                            keyEvent.key == Key.MoveEnd -> {
                                onSnapToMin()
                                true
                            }
                            else -> false
                        }
                    }
                    PanelResizeSide.Right -> {
                        when {
                            keyEvent.isCtrlPressed && keyEvent.key == Key.DirectionRight -> {
                                onSnapToMin()
                                true
                            }
                            keyEvent.key == Key.DirectionLeft -> {
                                onAdjustWidth(8.dp)
                                true
                            }
                            keyEvent.key == Key.DirectionRight -> {
                                onAdjustWidth((-8).dp)
                                true
                            }
                            keyEvent.key == Key.MoveHome -> {
                                onResetToDefault()
                                true
                            }
                            keyEvent.key == Key.MoveEnd -> {
                                onSnapToMin()
                                true
                            }
                            else -> false
                        }
                    }
                    PanelResizeSide.Bottom -> {
                        when {
                            keyEvent.isCtrlPressed && keyEvent.key == Key.DirectionDown -> {
                                onSnapToMin()
                                true
                            }
                            keyEvent.key == Key.DirectionUp -> {
                                onAdjustWidth(8.dp)
                                true
                            }
                            keyEvent.key == Key.DirectionDown -> {
                                onAdjustWidth((-8).dp)
                                true
                            }
                            keyEvent.key == Key.MoveHome -> {
                                onResetToDefault()
                                true
                            }
                            keyEvent.key == Key.MoveEnd -> {
                                onSnapToMin()
                                true
                            }
                            else -> false
                        }
                    }
                }
            }
            .pointerInput(side) {
                var lastTapTime = 0L
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    var dragStarted = false
                    var totalDragPx = 0f
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            // Pointer released
                            if (dragStarted) {
                                isDragging = false
                                onDragEnd()
                            } else {
                                if (downTime - lastTapTime <= 350L) {
                                    onDoubleClick()
                                    lastTapTime = 0L
                                } else {
                                    lastTapTime = downTime
                                }
                            }
                            break
                        }

                        val deltaPx = if (isHorizontal) {
                            change.position.x - change.previousPosition.x
                        } else {
                            change.position.y - change.previousPosition.y
                        }
                        totalDragPx += deltaPx

                        val sign = if (side == PanelResizeSide.Right || side == PanelResizeSide.Bottom) -1f else 1f

                        if (!dragStarted) {
                            if (kotlin.math.abs(totalDragPx) >= touchSlop) {
                                dragStarted = true
                                isDragging = true
                                lastTapTime = 0L
                                onDragStart()
                                change.consume()
                                val initialDeltaDp = with(density) { (totalDragPx * sign).toDp() }
                                onDragDelta(initialDeltaDp)
                            }
                        } else {
                            change.consume()
                            val deltaDp = with(density) { (deltaPx * sign).toDp() }
                            onDragDelta(deltaDp)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val isHighlighted = isHovered || isDragging || isFocused
        val lineWidth = if (isHighlighted) 2.dp else 1.dp
        val lineColor = when {
            isDragging -> MaterialTheme.colorScheme.primary
            isHovered || isFocused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
            else -> MaterialTheme.colorScheme.outlineVariant
        }

        Box(
            modifier = if (isHorizontal) {
                Modifier
                    .width(lineWidth)
                    .fillMaxHeight()
                    .background(lineColor)
            } else {
                Modifier
                    .fillMaxWidth()
                    .height(lineWidth)
                    .background(lineColor)
            }
        )
    }
}
