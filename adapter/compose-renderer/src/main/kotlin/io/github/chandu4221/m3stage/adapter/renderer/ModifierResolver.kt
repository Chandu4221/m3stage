package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.model.ModifierSpec

/**
 * Resolves a list of domain [ModifierSpec]s into a Compose [Modifier] chain.
 * Follows strict Compose chaining order.
 */
@Composable
fun Modifier.resolveModifiers(modifiers: List<ModifierSpec>): Modifier {
    var current: Modifier = this

    for (node in modifiers) {
        current = when (node) {
            is ModifierSpec.Padding -> current.padding(
                start = node.start.dp,
                top = node.top.dp,
                end = node.end.dp,
                bottom = node.bottom.dp
            )

            is ModifierSpec.FillMaxWidth -> current.fillMaxWidth(node.fraction)
            is ModifierSpec.FillMaxHeight -> current.fillMaxHeight(node.fraction)
            is ModifierSpec.FillMaxSize -> current.fillMaxSize(node.fraction)

            is ModifierSpec.Size -> current.size(node.width.dp, node.height.dp)
            is ModifierSpec.Width -> current.width(node.width.dp)
            is ModifierSpec.Height -> current.height(node.height.dp)
            is ModifierSpec.WrapContentSize -> current.wrapContentSize(unbounded = node.unbounded)

            is ModifierSpec.Background -> {
                val color = ThemeResolver.resolveColor(node.color)
                val shape = node.shape?.let { ThemeResolver.resolveShape(it) } ?: RectangleShape
                current.background(color = color, shape = shape)
            }

            is ModifierSpec.Border -> {
                val color = ThemeResolver.resolveColor(node.color)
                val shape = ThemeResolver.resolveShape(node.shape)
                current.border(width = node.width.dp, color = color, shape = shape)
            }

            is ModifierSpec.Clip -> {
                val shape = ThemeResolver.resolveShape(node.shape)
                current.clip(shape)
            }

            is ModifierSpec.Shadow -> {
                val shape = ThemeResolver.resolveShape(node.shape)
                current.shadow(elevation = node.elevation.dp, shape = shape, clip = node.clip)
            }

            is ModifierSpec.Alpha -> current.alpha(node.alpha)

            is ModifierSpec.Offset -> current.offset(x = node.x.dp, y = node.y.dp)
        }
    }

    return current
}