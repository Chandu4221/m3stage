package io.github.chandu4221.m3stage.adapter.codegen.resolver

import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

object ThemeCodeResolver {

    fun resolveColor(value: PropertyValue?): CodeBlock? {
        return when (value) {
            is PropertyValue.ColorValue.Token -> {
                when (value.token) {
                    M3ColorToken.Primary -> CodeBlock.of("%T.colorScheme.primary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnPrimary -> CodeBlock.of("%T.colorScheme.onPrimary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.PrimaryContainer -> CodeBlock.of("%T.colorScheme.primaryContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnPrimaryContainer -> CodeBlock.of("%T.colorScheme.onPrimaryContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Secondary -> CodeBlock.of("%T.colorScheme.secondary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnSecondary -> CodeBlock.of("%T.colorScheme.onSecondary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SecondaryContainer -> CodeBlock.of("%T.colorScheme.secondaryContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnSecondaryContainer -> CodeBlock.of("%T.colorScheme.onSecondaryContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Tertiary -> CodeBlock.of("%T.colorScheme.tertiary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnTertiary -> CodeBlock.of("%T.colorScheme.onTertiary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.TertiaryContainer -> CodeBlock.of("%T.colorScheme.tertiaryContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnTertiaryContainer -> CodeBlock.of("%T.colorScheme.onTertiaryContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Error -> CodeBlock.of("%T.colorScheme.error", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnError -> CodeBlock.of("%T.colorScheme.onError", ComposeSymbols.MaterialTheme)
                    M3ColorToken.ErrorContainer -> CodeBlock.of("%T.colorScheme.errorContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnErrorContainer -> CodeBlock.of("%T.colorScheme.onErrorContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Background -> CodeBlock.of("%T.colorScheme.background", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnBackground -> CodeBlock.of("%T.colorScheme.onBackground", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Surface -> CodeBlock.of("%T.colorScheme.surface", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnSurface -> CodeBlock.of("%T.colorScheme.onSurface", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceVariant -> CodeBlock.of("%T.colorScheme.surfaceVariant", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OnSurfaceVariant -> CodeBlock.of("%T.colorScheme.onSurfaceVariant", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Outline -> CodeBlock.of("%T.colorScheme.outline", ComposeSymbols.MaterialTheme)
                    M3ColorToken.OutlineVariant -> CodeBlock.of("%T.colorScheme.outlineVariant", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Scrim -> CodeBlock.of("%T.colorScheme.scrim", ComposeSymbols.MaterialTheme)
                    M3ColorToken.InverseSurface -> CodeBlock.of("%T.colorScheme.inverseSurface", ComposeSymbols.MaterialTheme)
                    M3ColorToken.InverseOnSurface -> CodeBlock.of("%T.colorScheme.inverseOnSurface", ComposeSymbols.MaterialTheme)
                    M3ColorToken.InversePrimary -> CodeBlock.of("%T.colorScheme.inversePrimary", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceDim -> CodeBlock.of("%T.colorScheme.surfaceDim", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceBright -> CodeBlock.of("%T.colorScheme.surfaceBright", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceContainerLowest -> CodeBlock.of("%T.colorScheme.surfaceContainerLowest", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceContainerLow -> CodeBlock.of("%T.colorScheme.surfaceContainerLow", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceContainer -> CodeBlock.of("%T.colorScheme.surfaceContainer", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceContainerHigh -> CodeBlock.of("%T.colorScheme.surfaceContainerHigh", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceContainerHighest -> CodeBlock.of("%T.colorScheme.surfaceContainerHighest", ComposeSymbols.MaterialTheme)
                    M3ColorToken.SurfaceTint -> CodeBlock.of("%T.colorScheme.surfaceTint", ComposeSymbols.MaterialTheme)
                    M3ColorToken.Inherit -> null
                }
            }
            is PropertyValue.ColorValue.Custom -> {
                val hexString = "0x" + value.argb.toULong().toString(16).uppercase()
                CodeBlock.of("%T(%L)", ComposeSymbols.Color, hexString)
            }
            else -> null
        }
    }

    fun resolveShape(value: PropertyValue?): CodeBlock? {
        return when (value) {
            is PropertyValue.ShapeValue.Token -> {
                when (value.token) {
                    M3ShapeToken.None -> CodeBlock.of("%T.shapes.extraSmall", ComposeSymbols.MaterialTheme)
                    M3ShapeToken.ExtraSmall -> CodeBlock.of("%T.shapes.extraSmall", ComposeSymbols.MaterialTheme)
                    M3ShapeToken.Small -> CodeBlock.of("%T.shapes.small", ComposeSymbols.MaterialTheme)
                    M3ShapeToken.Medium -> CodeBlock.of("%T.shapes.medium", ComposeSymbols.MaterialTheme)
                    M3ShapeToken.Large -> CodeBlock.of("%T.shapes.large", ComposeSymbols.MaterialTheme)
                    M3ShapeToken.ExtraLarge -> CodeBlock.of("%T.shapes.extraLarge", ComposeSymbols.MaterialTheme)
                    M3ShapeToken.Full -> CodeBlock.of("%T.shapes.extraLarge", ComposeSymbols.MaterialTheme)
                }
            }
            is PropertyValue.ShapeValue.UniformDp -> {
                CodeBlock.of("%T(%L.dp)", ComposeSymbols.RoundedCornerShape, value.cornerRadius)
            }
            is PropertyValue.ShapeValue.CornerDp -> {
                CodeBlock.of(
                    "%T(topStart = %L.dp, topEnd = %L.dp, bottomEnd = %L.dp, bottomStart = %L.dp)",
                    ComposeSymbols.RoundedCornerShape,
                    value.topStart,
                    value.topEnd,
                    value.bottomEnd,
                    value.bottomStart
                )
            }
            else -> null
        }
    }

    fun resolveTypography(value: PropertyValue?): CodeBlock? {
        return when (value) {
            is PropertyValue.TypographyValue.Token -> {
                when (value.token) {
                    M3TypographyToken.DisplayLarge -> CodeBlock.of("%T.typography.displayLarge", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.DisplayMedium -> CodeBlock.of("%T.typography.displayMedium", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.DisplaySmall -> CodeBlock.of("%T.typography.displaySmall", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.HeadlineLarge -> CodeBlock.of("%T.typography.headlineLarge", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.HeadlineMedium -> CodeBlock.of("%T.typography.headlineMedium", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.HeadlineSmall -> CodeBlock.of("%T.typography.headlineSmall", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.TitleLarge -> CodeBlock.of("%T.typography.titleLarge", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.TitleMedium -> CodeBlock.of("%T.typography.titleMedium", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.TitleSmall -> CodeBlock.of("%T.typography.titleSmall", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.BodyLarge -> CodeBlock.of("%T.typography.bodyLarge", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.BodyMedium -> CodeBlock.of("%T.typography.bodyMedium", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.BodySmall -> CodeBlock.of("%T.typography.bodySmall", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.LabelLarge -> CodeBlock.of("%T.typography.labelLarge", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.LabelMedium -> CodeBlock.of("%T.typography.labelMedium", ComposeSymbols.MaterialTheme)
                    M3TypographyToken.LabelSmall -> CodeBlock.of("%T.typography.labelSmall", ComposeSymbols.MaterialTheme)
                }
            }
            else -> null
        }
    }
}
