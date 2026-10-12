package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectionOverlay(
    isSelected: Boolean,
    isLocked: Boolean,
    isHovered: Boolean = false,
    onHover: () -> Unit = {},
    onExit: () -> Unit = {},
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val (borderWidth, borderColor) = when {
        isLocked -> 2.dp to MaterialTheme.colorScheme.tertiary
        isSelected -> 2.dp to MaterialTheme.colorScheme.primary
        isHovered -> 1.5.dp to MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
        else -> 0.dp to Color.Transparent
    }

    Box(
        modifier = Modifier
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .semantics { role = Role.Button }
            .onPointerEvent(PointerEventType.Move) {
                onHover()
            }
            .onPointerEvent(PointerEventType.Exit) {
                onExit()
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}