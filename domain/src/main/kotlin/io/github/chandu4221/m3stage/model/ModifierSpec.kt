package io.github.chandu4221.m3stage.model

import io.github.chandu4221.m3stage.property.PropertyValue

/**
 * Declarative specification of visual Compose modifiers.
 * Excludes behavioral and logic-attaching modifiers (Clickable, PointerInput, Focus, etc.).
 */
sealed interface ModifierSpec {
    val category: ModifierCategory

    // =========================================================================
    // 1. PADDING
    // =========================================================================

    data class Padding(
        val start: Float = 0f,
        val top: Float = 0f,
        val end: Float = 0f,
        val bottom: Float = 0f
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Padding

        companion object {
            fun all(value: Float) = Padding(start = value, top = value, end = value, bottom = value)
            fun symmetric(horizontal: Float = 0f, vertical: Float = 0f) =
                Padding(start = horizontal, top = vertical, end = horizontal, bottom = vertical)
        }
    }

    // =========================================================================
    // 2. SIZE
    // =========================================================================

    data class FillMaxWidth(val fraction: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    data class FillMaxHeight(val fraction: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    data class FillMaxSize(val fraction: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    data class Size(val width: Float, val height: Float) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    data class Width(val width: Float) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    data class Height(val height: Float) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    data class WrapContentSize(val unbounded: Boolean = false) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    // =========================================================================
    // 3. DRAWING & APPEARANCE (Drawing, Border, Graphics)
    // =========================================================================

    data class Background(
        val color: PropertyValue.ColorValue,
        val shape: PropertyValue.ShapeValue? = null
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Drawing
    }

    data class Border(
        val width: Float = 1f,
        val color: PropertyValue.ColorValue,
        val shape: PropertyValue.ShapeValue = PropertyValue.ShapeValue.UniformDp(0f)
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Drawing
    }

    data class Clip(
        val shape: PropertyValue.ShapeValue
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Drawing
    }

    data class Shadow(
        val elevation: Float = 4f,
        val shape: PropertyValue.ShapeValue = PropertyValue.ShapeValue.UniformDp(0f),
        val clip: Boolean = false
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Drawing
    }

    data class Alpha(
        val alpha: Float = 1f
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Drawing
    }

    // =========================================================================
    // 4. POSITION & ALIGNMENT
    // =========================================================================

    data class Offset(
        val x: Float = 0f,
        val y: Float = 0f
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Position
    }
}