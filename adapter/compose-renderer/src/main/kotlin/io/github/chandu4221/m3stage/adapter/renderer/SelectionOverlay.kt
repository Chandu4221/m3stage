package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun SelectionOverlay(
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor = when {
        isLocked -> MaterialTheme.colorScheme.tertiary
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .border(
                width = if (isSelected || isLocked) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .semantics { role = Role.Button }
            .clickable(onClick = onClick)
    ) {
        content()
    }
}