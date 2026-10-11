package io.github.chandu4221.m3stage.adapter.renderer.resolver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.chandu4221.m3stage.modifier.ModifierSpec

/**
 * Resolves a list of domain [ModifierSpec]s into a Compose [Modifier] chain.
 * Follows strict Compose chaining order.
 */
@Composable
fun Modifier.resolveModifiers(modifiers: List<ModifierSpec>): Modifier {
    var current: Modifier = this

    for (node in modifiers) {
        current = when (node) {
            // 1. Padding
            is ModifierSpec.Padding -> current.padding(
                start = node.start.dp,
                top = node.top.dp,
                end = node.end.dp,
                bottom = node.bottom.dp
            )

            // 2. Size
            is ModifierSpec.FillMaxWidth -> current.fillMaxWidth(node.fraction)
            is ModifierSpec.FillMaxHeight -> current.fillMaxHeight(node.fraction)
            is ModifierSpec.FillMaxSize -> current.fillMaxSize(node.fraction)

            is ModifierSpec.Size -> current.size(node.width.dp, node.height.dp)
            is ModifierSpec.Width -> current.width(node.width.dp)
            is ModifierSpec.Height -> current.height(node.height.dp)
            is ModifierSpec.WrapContentSize -> current.wrapContentSize(unbounded = node.unbounded)
            is ModifierSpec.Weight -> current // In non-scope context, weight behaves as pass-through
            is ModifierSpec.AspectRatio -> current.aspectRatio(node.ratio)

            // 3. Position & Order
            is ModifierSpec.Offset -> current.offset(x = node.x.dp, y = node.y.dp)
            is ModifierSpec.ZIndex -> current.zIndex(node.zIndex)

            // 4. Drawing & Border
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

            is ModifierSpec.Shadow -> {
                val shape = ThemeResolver.resolveShape(node.shape)
                current.shadow(elevation = node.elevation.dp, shape = shape, clip = node.clip)
            }

            // 5. Graphics Layer
            is ModifierSpec.Clip -> {
                val shape = ThemeResolver.resolveShape(node.shape)
                current.clip(shape)
            }

            is ModifierSpec.Alpha -> current.alpha(node.alpha)

            // 6. Transformations
            is ModifierSpec.Rotate -> current.rotate(node.degrees)
            is ModifierSpec.Scale -> current.scale(node.scale)

            // 7. Scroll
            is ModifierSpec.VerticalScroll -> {
                if (node.enabled) {
                    val scrollState = rememberScrollState()
                    current.verticalScroll(scrollState)
                } else current
            }

            is ModifierSpec.HorizontalScroll -> {
                if (node.enabled) {
                    val scrollState = rememberScrollState()
                    current.horizontalScroll(scrollState)
                } else current
            }
        }
    }

    return current
}
