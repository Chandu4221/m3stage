package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Interactive canvas placeholder rendered inside empty layout containers (Column, Row, Box, Card).
 *
 * Prevents zero-size collapse on the visual canvas and provides a clear click target
 * for selecting the container and dropping child components.
 * This is strictly a canvas-only visual aid and is NEVER emitted into generated Kotlin code.
 */
@Composable
fun EmptyContainerPlaceholder(
    containerName: String, modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val cornerRadius = 6.dp

    Box(
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp).padding(4.dp).background(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
            shape = RoundedCornerShape(cornerRadius)
        ).drawBehind {
            val strokeWidth = 1.5.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            drawRoundRect(
                color = outlineColor, style = Stroke(
                    width = strokeWidth, pathEffect = dashEffect
                ), cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx())
            )
        }.padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "+ Empty $containerName — Add Composables",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}