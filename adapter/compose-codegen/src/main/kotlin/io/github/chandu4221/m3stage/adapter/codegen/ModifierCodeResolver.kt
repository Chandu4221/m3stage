package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.ModifierSpec

object ModifierCodeResolver {

    fun generateModifierChain(modifiers: List<ModifierSpec>): CodeBlock? {
        if (modifiers.isEmpty()) return null

        val builder = CodeBlock.builder()
        builder.add("Modifier")

        for (node in modifiers) {
            when (node) {
                is ModifierSpec.Padding -> {
                    if (node.start == node.end && node.top == node.bottom && node.start == node.top) {
                        builder.add("\n.padding(%L.dp)", node.start)
                    } else if (node.start == node.end && node.top == node.bottom) {
                        builder.add("\n.padding(horizontal = %L.dp, vertical = %L.dp)", node.start, node.top)
                    } else {
                        builder.add(
                            "\n.padding(start = %L.dp, top = %L.dp, end = %L.dp, bottom = %L.dp)",
                            node.start,
                            node.top,
                            node.end,
                            node.bottom
                        )
                    }
                }

                is ModifierSpec.FillMaxWidth -> {
                    if (node.fraction == 1f) {
                        builder.add("\n.fillMaxWidth()")
                    } else {
                        builder.add("\n.fillMaxWidth(%Lf)", node.fraction)
                    }
                }

                is ModifierSpec.FillMaxHeight -> {
                    if (node.fraction == 1f) {
                        builder.add("\n.fillMaxHeight()")
                    } else {
                        builder.add("\n.fillMaxHeight(%Lf)", node.fraction)
                    }
                }

                is ModifierSpec.FillMaxSize -> {
                    if (node.fraction == 1f) {
                        builder.add("\n.fillMaxSize()")
                    } else {
                        builder.add("\n.fillMaxSize(%Lf)", node.fraction)
                    }
                }

                is ModifierSpec.Size -> {
                    builder.add("\n.size(width = %L.dp, height = %L.dp)", node.width, node.height)
                }

                is ModifierSpec.Width -> {
                    builder.add("\n.width(%L.dp)", node.width)
                }

                is ModifierSpec.Height -> {
                    builder.add("\n.height(%L.dp)", node.height)
                }

                is ModifierSpec.WrapContentSize -> {
                    if (node.unbounded) {
                        builder.add("\n.wrapContentSize(unbounded = true)")
                    } else {
                        builder.add("\n.wrapContentSize()")
                    }
                }

                is ModifierSpec.Background -> {
                    val colorCode = ThemeCodeResolver.resolveColor(node.color)
                    if (node.shape != null) {
                        val shapeCode = ThemeCodeResolver.resolveShape(node.shape)
                        builder.add("\n.background(color = %L, shape = %L)", colorCode, shapeCode)
                    } else {
                        builder.add("\n.background(%L)", colorCode)
                    }
                }

                is ModifierSpec.Border -> {
                    val colorCode = ThemeCodeResolver.resolveColor(node.color)
                    val shapeCode = ThemeCodeResolver.resolveShape(node.shape)
                    builder.add("\n.border(width = %L.dp, color = %L, shape = %L)", node.width, colorCode, shapeCode)
                }

                is ModifierSpec.Clip -> {
                    val shapeCode = ThemeCodeResolver.resolveShape(node.shape)
                    builder.add("\n.clip(%L)", shapeCode)
                }

                is ModifierSpec.Shadow -> {
                    val shapeCode = ThemeCodeResolver.resolveShape(node.shape)
                    if (node.clip) {
                        builder.add("\n.shadow(elevation = %L.dp, shape = %L, clip = true)", node.elevation, shapeCode)
                    } else {
                        builder.add("\n.shadow(elevation = %L.dp, shape = %L)", node.elevation, shapeCode)
                    }
                }

                is ModifierSpec.Alpha -> {
                    builder.add("\n.alpha(%Lf)", node.alpha)
                }

                is ModifierSpec.Offset -> {
                    builder.add("\n.offset(x = %L.dp, y = %L.dp)", node.x, node.y)
                }
            }
        }

        return builder.build()
    }
}