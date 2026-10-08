package io.github.chandu4221.m3stage.ui.theme

import androidx.compose.ui.graphics.Color

data class SeedColorPreset(
    val name: String,
    val argb: Long,
    val color: Color = Color(argb)
)

object SeedColorPresets {
    val BaselinePurple = SeedColorPreset("M3 Baseline Purple", 0xFF6750A4L)
    val EmeraldGreen = SeedColorPreset("Emerald Green", 0xFF006C4CL)
    val SapphireBlue = SeedColorPreset("Sapphire Blue", 0xFF0061A4L)
    val CoralRed = SeedColorPreset("Coral Red", 0xFFB3261EL)
    val SunsetOrange = SeedColorPreset("Sunset Amber", 0xFF825500L)
    val RosePink = SeedColorPreset("Rose Pink", 0xFF984061L)
    val OceanTeal = SeedColorPreset("Ocean Teal", 0xFF006874L)
    val SlateMono = SeedColorPreset("Slate Monochrome", 0xFF4E5E6DL)

    val all = listOf(
        BaselinePurple,
        EmeraldGreen,
        SapphireBlue,
        OceanTeal,
        CoralRed,
        SunsetOrange,
        RosePink,
        SlateMono
    )
}