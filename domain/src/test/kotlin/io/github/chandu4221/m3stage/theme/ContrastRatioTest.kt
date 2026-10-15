package io.github.chandu4221.m3stage.theme

import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertTrue

class ContrastRatioTest {

    private fun relativeLuminance(rgbHex: Long): Double {
        fun channel(c: Int): Double {
            val s = c / 255.0
            return if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        val r = ((rgbHex shr 16) and 0xFF).toInt()
        val g = ((rgbHex shr 8) and 0xFF).toInt()
        val b = (rgbHex and 0xFF).toInt()
        return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)
    }

    private fun contrastRatio(color1Hex: Long, color2Hex: Long): Double {
        val lum1 = relativeLuminance(color1Hex)
        val lum2 = relativeLuminance(color2Hex)
        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun testEmptyContainerPlaceholderContrastMeetsWcagAAInBothThemes() {
        // Light Theme: onSurface (#1D1B20) on surfaceContainerHighest (#E6E0E9)
        val lightOnSurface = 0x1D1B20L
        val lightSurfaceContainerHighest = 0xE6E0E9L
        val lightRatio = contrastRatio(lightOnSurface, lightSurfaceContainerHighest)
        assertTrue(
            lightRatio >= 4.5,
            "Light mode EmptyContainerPlaceholder contrast ($lightRatio) must be >= 4.5:1 (WCAG AA)"
        )

        // Dark Theme: onSurface (#E6E1E5) on surfaceContainerHighest (#36343B)
        val darkOnSurface = 0xE6E1E5L
        val darkSurfaceContainerHighest = 0x36343BL
        val darkRatio = contrastRatio(darkOnSurface, darkSurfaceContainerHighest)
        assertTrue(
            darkRatio >= 4.5,
            "Dark mode EmptyContainerPlaceholder contrast ($darkRatio) must be >= 4.5:1 (WCAG AA)"
        )
    }

    @Test
    fun testFilledButtonLabelContrastMeetsWcagAAInBothThemes() {
        // Light Theme: onPrimary (#FFFFFF) on primary (#6750A4)
        val lightOnPrimary = 0xFFFFFFL
        val lightPrimary = 0x6750A4L
        val lightButtonRatio = contrastRatio(lightOnPrimary, lightPrimary)
        assertTrue(
            lightButtonRatio >= 4.5,
            "Light mode Button label contrast ($lightButtonRatio) must be >= 4.5:1 (WCAG AA)"
        )

        // Dark Theme: onPrimary (#381E72) on primary (#D0BCFF)
        val darkOnPrimary = 0x381E72L
        val darkPrimary = 0xD0BCFFL
        val darkButtonRatio = contrastRatio(darkOnPrimary, darkPrimary)
        assertTrue(
            darkButtonRatio >= 4.5,
            "Dark mode Button label contrast ($darkButtonRatio) must be >= 4.5:1 (WCAG AA)"
        )
    }

    @Test
    fun testNavigationBarIndicatorAndIconContrastMeetsWcagNonTextInBothThemes() {
        // Light Theme: onSecondaryContainer (#1D192B) on secondaryContainer (#E8DEF8)
        val lightSelectedIcon = 0x1D192BL
        val lightSelectedIndicator = 0xE8DEF8L
        val lightIconRatio = contrastRatio(lightSelectedIcon, lightSelectedIndicator)
        assertTrue(
            lightIconRatio >= 3.0,
            "Light mode Nav indicator/icon contrast ($lightIconRatio) must be >= 3.0:1 (WCAG Non-text)"
        )

        // Dark Theme: onSecondaryContainer (#E8DEF8) on secondaryContainer (#4A4458)
        val darkSelectedIcon = 0xE8DEF8L
        val darkSelectedIndicator = 0x4A4458L
        val darkIconRatio = contrastRatio(darkSelectedIcon, darkSelectedIndicator)
        assertTrue(
            darkIconRatio >= 3.0,
            "Dark mode Nav indicator/icon contrast ($darkIconRatio) must be >= 3.0:1 (WCAG Non-text)"
        )
    }
}
