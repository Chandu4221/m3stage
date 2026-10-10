package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.ModifierNode

/**
 * Resolves a list of domain [ModifierNode]s into a Compose [Modifier] chain.
 * Follows strict Compose chaining order.
 */
@Composable
fun Modifier.resolveModifiers(modifiers: List<ModifierNode>): Modifier {
    var current: Modifier = this

    for (node in modifiers) {
        current = when (node) {
            is ModifierNode.Padding -> current.padding(
                start = node.start.dp,
                top = node.top.dp,
                end = node.end.dp,
                bottom = node.bottom.dp
            )

            is ModifierNode.FillMaxWidth -> current.fillMaxWidth(node.fraction)
            is ModifierNode.FillMaxHeight -> current.fillMaxHeight(node.fraction)
            is ModifierNode.FillMaxSize -> current.fillMaxSize(node.fraction)

            is ModifierNode.Size -> current.size(node.width.dp, node.height.dp)
            is ModifierNode.Width -> current.width(node.width.dp)
            is ModifierNode.Height -> current.height(node.height.dp)
            is ModifierNode.WrapContentSize -> current.wrapContentSize(unbounded = node.unbounded)

            is ModifierNode.Background -> {
                val color = ThemeResolver.resolveColor(node.color)
                val shape = node.shape?.let { ThemeResolver.resolveShape(it) } ?: RectangleShape
                current.background(color = color, shape = shape)
            }

            is ModifierNode.Border -> {
                val color = ThemeResolver.resolveColor(node.color)
                val shape = ThemeResolver.resolveShape(node.shape)
                current.border(width = node.width.dp, color = color, shape = shape)
            }

            is ModifierNode.Clip -> {
                val shape = ThemeResolver.resolveShape(node.shape)
                current.clip(shape)
            }

            is ModifierNode.Shadow -> {
                val shape = ThemeResolver.resolveShape(node.shape)
                current.shadow(elevation = node.elevation.dp, shape = shape, clip = node.clip)
            }

            is ModifierNode.Alpha -> current.alpha(node.alpha)

            is ModifierNode.Clickable -> {
                if (node.enabled) {
                    current.clickable { /* preview preview stub */ }
                } else current
            }

            is ModifierNode.Offset -> current.offset(x = node.x.dp, y = node.y.dp)
        }
    }

    return current
}