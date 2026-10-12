package io.github.chandu4221.m3stage.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

val RobotoFlexFontFamily = FontFamily(
    // Point to the exact path relative to src/main/resources
    Font(
        resource = "font/roboto_flex.ttf",
        weight = FontWeight.Normal,
    ),
    Font(
        resource = "font/roboto_flex.ttf",
        weight = FontWeight.Medium,
    ),
    Font(
        resource = "font/roboto_flex.ttf",
        weight = FontWeight.Bold,
    ),
)


private fun TextStyle.withFont(fontFamily: FontFamily): TextStyle =
    this.copy(fontFamily = fontFamily)

fun createRobotoTypography(fontFamily: FontFamily = RobotoFlexFontFamily): Typography {
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.withFont(fontFamily),
        displayMedium = default.displayMedium.withFont(fontFamily),
        displaySmall = default.displaySmall.withFont(fontFamily),
        headlineLarge = default.headlineLarge.withFont(fontFamily),
        headlineMedium = default.headlineMedium.withFont(fontFamily),
        headlineSmall = default.headlineSmall.withFont(fontFamily),
        titleLarge = default.titleLarge.withFont(fontFamily),
        titleMedium = default.titleMedium.withFont(fontFamily),
        titleSmall = default.titleSmall.withFont(fontFamily),
        bodyLarge = default.bodyLarge.withFont(fontFamily),
        bodyMedium = default.bodyMedium.withFont(fontFamily),
        bodySmall = default.bodySmall.withFont(fontFamily),
        labelLarge = default.labelLarge.withFont(fontFamily),
        labelMedium = default.labelMedium.withFont(fontFamily),
        labelSmall = default.labelSmall.withFont(fontFamily),
    )
}