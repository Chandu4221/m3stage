package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

object ThemeCodeResolver {

    fun resolveColor(colorVal: PropertyValue.ColorValue?): CodeBlock? = when (colorVal) {
        is PropertyValue.ColorValue.Token -> resolveColorToken(colorVal.token)
        is PropertyValue.ColorValue.Custom -> {
            val hex = "0x" + java.lang.Long.toHexString(colorVal.argb).uppercase()
            CodeBlock.of("%T(%L)", ComposeSymbols.Color, hex)
        }
        null -> null
    }

    fun resolveColorToken(token: M3ColorToken): CodeBlock {
        val prop = when (token) {
            M3ColorToken.Primary -> "primary"
            M3ColorToken.OnPrimary -> "onPrimary"
            M3ColorToken.PrimaryContainer -> "primaryContainer"
            M3ColorToken.OnPrimaryContainer -> "onPrimaryContainer"
            M3ColorToken.InversePrimary -> "inversePrimary"

            M3ColorToken.Secondary -> "secondary"
            M3ColorToken.OnSecondary -> "onSecondary"
            M3ColorToken.SecondaryContainer -> "secondaryContainer"
            M3ColorToken.OnSecondaryContainer -> "onSecondaryContainer"

            M3ColorToken.Tertiary -> "tertiary"
            M3ColorToken.OnTertiary -> "onTertiary"
            M3ColorToken.TertiaryContainer -> "tertiaryContainer"
            M3ColorToken.OnTertiaryContainer -> "onTertiaryContainer"

            M3ColorToken.Background -> "background"
            M3ColorToken.OnBackground -> "onBackground"

            M3ColorToken.Surface -> "surface"
            M3ColorToken.OnSurface -> "onSurface"
            M3ColorToken.SurfaceVariant -> "surfaceVariant"
            M3ColorToken.OnSurfaceVariant -> "onSurfaceVariant"
            M3ColorToken.SurfaceTint -> "surfaceTint"
            M3ColorToken.InverseSurface -> "inverseSurface"
            M3ColorToken.InverseOnSurface -> "inverseOnSurface"

            M3ColorToken.SurfaceDim -> "surfaceDim"
            M3ColorToken.SurfaceBright -> "surfaceBright"
            M3ColorToken.SurfaceContainerLowest -> "surfaceContainerLowest"
            M3ColorToken.SurfaceContainerLow -> "surfaceContainerLow"
            M3ColorToken.SurfaceContainer -> "surfaceContainer"
            M3ColorToken.SurfaceContainerHigh -> "surfaceContainerHigh"
            M3ColorToken.SurfaceContainerHighest -> "surfaceContainerHighest"

            M3ColorToken.Error -> "error"
            M3ColorToken.OnError -> "onError"
            M3ColorToken.ErrorContainer -> "errorContainer"
            M3ColorToken.OnErrorContainer -> "onErrorContainer"
            M3ColorToken.Outline -> "outline"
            M3ColorToken.OutlineVariant -> "outlineVariant"
            M3ColorToken.Scrim -> "scrim"
        }
        return CodeBlock.of("%T.colorScheme.%L", ComposeSymbols.MaterialTheme, prop)
    }

    fun resolveTypography(typoVal: PropertyValue.TypographyValue?): CodeBlock? = when (typoVal) {
        is PropertyValue.TypographyValue.Token -> resolveTypographyToken(typoVal.token)
        null -> null
    }

    fun resolveTypographyToken(token: M3TypographyToken): CodeBlock {
        val prop = when (token) {
            M3TypographyToken.DisplayLarge -> "displayLarge"
            M3TypographyToken.DisplayMedium -> "displayMedium"
            M3TypographyToken.DisplaySmall -> "displaySmall"
            M3TypographyToken.HeadlineLarge -> "headlineLarge"
            M3TypographyToken.HeadlineMedium -> "headlineMedium"
            M3TypographyToken.HeadlineSmall -> "headlineSmall"
            M3TypographyToken.TitleLarge -> "titleLarge"
            M3TypographyToken.TitleMedium -> "titleMedium"
            M3TypographyToken.TitleSmall -> "titleSmall"
            M3TypographyToken.BodyLarge -> "bodyLarge"
            M3TypographyToken.BodyMedium -> "bodyMedium"
            M3TypographyToken.BodySmall -> "bodySmall"
            M3TypographyToken.LabelLarge -> "labelLarge"
            M3TypographyToken.LabelMedium -> "labelMedium"
            M3TypographyToken.LabelSmall -> "labelSmall"
        }
        return CodeBlock.of("%T.typography.%L", ComposeSymbols.MaterialTheme, prop)
    }

    fun resolveShape(shapeVal: PropertyValue.ShapeValue?): CodeBlock? = when (shapeVal) {
        is PropertyValue.ShapeValue.Token -> resolveShapeToken(shapeVal.token)
        is PropertyValue.ShapeValue.UniformDp -> CodeBlock.of(
            "%T(%L.%M)",
            ComposeSymbols.RoundedCornerShape,
            shapeVal.cornerRadius,
            ComposeSymbols.Dp
        )
        is PropertyValue.ShapeValue.CornerDp -> CodeBlock.of(
            "%T(topStart = %L.%M, topEnd = %L.%M, bottomEnd = %L.%M, bottomStart = %L.%M)",
            ComposeSymbols.RoundedCornerShape,
            shapeVal.topStart, ComposeSymbols.Dp,
            shapeVal.topEnd, ComposeSymbols.Dp,
            shapeVal.bottomEnd, ComposeSymbols.Dp,
            shapeVal.bottomStart, ComposeSymbols.Dp
        )
        null -> null
    }

    fun resolveShapeToken(token: M3ShapeToken): CodeBlock = when (token) {
        M3ShapeToken.None -> CodeBlock.of("%T(0.%M)", ComposeSymbols.RoundedCornerShape, ComposeSymbols.Dp)
        M3ShapeToken.ExtraSmall -> CodeBlock.of("%T.shapes.extraSmall", ComposeSymbols.MaterialTheme)
        M3ShapeToken.Small -> CodeBlock.of("%T.shapes.small", ComposeSymbols.MaterialTheme)
        M3ShapeToken.Medium -> CodeBlock.of("%T.shapes.medium", ComposeSymbols.MaterialTheme)
        M3ShapeToken.Large -> CodeBlock.of("%T.shapes.large", ComposeSymbols.MaterialTheme)
        M3ShapeToken.ExtraLarge -> CodeBlock.of("%T.shapes.extraLarge", ComposeSymbols.MaterialTheme)
        M3ShapeToken.Full -> CodeBlock.of("%T(50)", ComposeSymbols.RoundedCornerShape)
    }
}