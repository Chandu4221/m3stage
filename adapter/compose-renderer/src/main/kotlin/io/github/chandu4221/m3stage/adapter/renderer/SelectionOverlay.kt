package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectionOverlay(
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor = when {
        isLocked -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange for locked
        isSelected -> androidx.compose.ui.graphics.Color.Blue
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    val borderStyle = if (isLocked) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null

    Box(
        modifier = Modifier
            .then(
                if (borderStyle != null) Modifier.border(
                    borderStyle,
                    androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                else Modifier.border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = borderColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
            )
            .clickable(
                // If locked, we can either block the click or allow it to select but show it's locked.
                // Allowing selection so the user can unlock it is usually better UX.
                onClick = onClick
            )
    ) {
        content()
    }
}