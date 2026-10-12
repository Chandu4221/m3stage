package io.github.chandu4221.m3stage.modifier

import io.github.chandu4221.m3stage.property.PropertyValue

enum class ModifierScope {
    Any,
    BoxParent,
    RowColumnParent
}

/**
 * Declarative specification of visual Compose modifiers.
 * Excludes behavioral and logic-attaching modifiers (Clickable, PointerInput, Focus, etc.).
 */
sealed interface ModifierSpec {
    val category: ModifierCategory
    val scope: ModifierScope get() = ModifierScope.Any

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

    data class Weight(val weight: Float = 1f, val fill: Boolean = true) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
        override val scope: ModifierScope get() = ModifierScope.RowColumnParent
    }

    data class AspectRatio(val ratio: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Size
    }

    // =========================================================================
    // 3. POSITION & ORDER
    // =========================================================================

    data class Offset(val x: Float = 0f, val y: Float = 0f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Position
    }

    data class ZIndex(val zIndex: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Position
        override val scope: ModifierScope get() = ModifierScope.BoxParent
    }

    // =========================================================================
    // 4. DRAWING & BORDER
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

    data class Shadow(
        val elevation: Float = 4f,
        val shape: PropertyValue.ShapeValue = PropertyValue.ShapeValue.UniformDp(0f),
        val clip: Boolean = false
    ) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Drawing
    }

    // =========================================================================
    // 5. GRAPHICS LAYER
    // =========================================================================

    data class Clip(val shape: PropertyValue.ShapeValue) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Graphics
    }

    data class Alpha(val alpha: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Graphics
    }

    // =========================================================================
    // 6. TRANSFORMATIONS
    // =========================================================================

    data class Rotate(val degrees: Float = 0f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Transformations
    }

    data class Scale(val scale: Float = 1f) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Transformations
    }

    // =========================================================================
    // 7. SCROLL
    // =========================================================================

    data class VerticalScroll(val enabled: Boolean = true) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Scroll
    }

    data class HorizontalScroll(val enabled: Boolean = true) : ModifierSpec {
        override val category: ModifierCategory get() = ModifierCategory.Scroll
    }
}