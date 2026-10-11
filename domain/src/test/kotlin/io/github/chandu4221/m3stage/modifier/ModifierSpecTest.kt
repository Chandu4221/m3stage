package io.github.chandu4221.m3stage.modifier

import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import kotlin.test.Test
import kotlin.test.assertEquals

class ModifierSpecTest {

    @Test
    fun testModifierCategoriesAreCorrect() {
        assertEquals(ModifierCategory.Padding, ModifierSpec.Padding().category)
        assertEquals(ModifierCategory.Size, ModifierSpec.FillMaxSize().category)
        assertEquals(ModifierCategory.Size, ModifierSpec.Weight(1f).category)
        assertEquals(ModifierCategory.Size, ModifierSpec.AspectRatio(16f / 9f).category)
        assertEquals(ModifierCategory.Position, ModifierSpec.Offset(10f, 20f).category)
        assertEquals(ModifierCategory.Position, ModifierSpec.ZIndex(2f).category)
        assertEquals(ModifierCategory.Drawing, ModifierSpec.Background(PropertyValue.ColorValue.Token(M3ColorToken.Primary)).category)
        assertEquals(ModifierCategory.Graphics, ModifierSpec.Alpha(0.5f).category)
        assertEquals(ModifierCategory.Transformations, ModifierSpec.Rotate(45f).category)
        assertEquals(ModifierCategory.Scroll, ModifierSpec.VerticalScroll(true).category)
    }

    @Test
    fun testPaddingFactories() {
        val allPadding = ModifierSpec.Padding.all(16f)
        assertEquals(16f, allPadding.start)
        assertEquals(16f, allPadding.top)
        assertEquals(16f, allPadding.end)
        assertEquals(16f, allPadding.bottom)

        val symmetricPadding = ModifierSpec.Padding.symmetric(horizontal = 24f, vertical = 12f)
        assertEquals(24f, symmetricPadding.start)
        assertEquals(24f, symmetricPadding.end)
        assertEquals(12f, symmetricPadding.top)
        assertEquals(12f, symmetricPadding.bottom)
    }
}