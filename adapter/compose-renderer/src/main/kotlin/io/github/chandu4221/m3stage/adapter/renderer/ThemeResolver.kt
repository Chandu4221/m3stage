package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

object ThemeResolver {

    @Composable
    @ReadOnlyComposable
    fun resolveColor(colorValue: PropertyValue.ColorValue?): Color {
        return when (colorValue) {
            is PropertyValue.ColorValue.Custom -> Color(colorValue.argb)
            is PropertyValue.ColorValue.Token -> resolveColorToken(colorValue.token)
            null -> Color.Unspecified
        }
    }

    @Composable
    @ReadOnlyComposable
    fun resolveColorToken(token: M3ColorToken): Color {
        val scheme = MaterialTheme.colorScheme
        return when (token) {
            M3ColorToken.Primary -> scheme.primary
            M3ColorToken.OnPrimary -> scheme.onPrimary
            M3ColorToken.PrimaryContainer -> scheme.primaryContainer
            M3ColorToken.OnPrimaryContainer -> scheme.onPrimaryContainer
            M3ColorToken.InversePrimary -> scheme.inversePrimary

            M3ColorToken.Secondary -> scheme.secondary
            M3ColorToken.OnSecondary -> scheme.onSecondary
            M3ColorToken.SecondaryContainer -> scheme.secondaryContainer
            M3ColorToken.OnSecondaryContainer -> scheme.onSecondaryContainer

            M3ColorToken.Tertiary -> scheme.tertiary
            M3ColorToken.OnTertiary -> scheme.onTertiary
            M3ColorToken.TertiaryContainer -> scheme.tertiaryContainer
            M3ColorToken.OnTertiaryContainer -> scheme.onTertiaryContainer

            M3ColorToken.Background -> scheme.background
            M3ColorToken.OnBackground -> scheme.onBackground

            M3ColorToken.Surface -> scheme.surface
            M3ColorToken.OnSurface -> scheme.onSurface
            M3ColorToken.SurfaceVariant -> scheme.surfaceVariant
            M3ColorToken.OnSurfaceVariant -> scheme.onSurfaceVariant
            M3ColorToken.SurfaceTint -> scheme.surfaceTint
            M3ColorToken.InverseSurface -> scheme.inverseSurface
            M3ColorToken.InverseOnSurface -> scheme.inverseOnSurface

            M3ColorToken.SurfaceDim -> scheme.surfaceDim
            M3ColorToken.SurfaceBright -> scheme.surfaceBright
            M3ColorToken.SurfaceContainerLowest -> scheme.surfaceContainerLowest
            M3ColorToken.SurfaceContainerLow -> scheme.surfaceContainerLow
            M3ColorToken.SurfaceContainer -> scheme.surfaceContainer
            M3ColorToken.SurfaceContainerHigh -> scheme.surfaceContainerHigh
            M3ColorToken.SurfaceContainerHighest -> scheme.surfaceContainerHighest

            M3ColorToken.Error -> scheme.error
            M3ColorToken.OnError -> scheme.onError
            M3ColorToken.ErrorContainer -> scheme.errorContainer
            M3ColorToken.OnErrorContainer -> scheme.onErrorContainer
            M3ColorToken.Outline -> scheme.outline
            M3ColorToken.OutlineVariant -> scheme.outlineVariant
            M3ColorToken.Scrim -> scheme.scrim
        }
    }

    @Composable
    @ReadOnlyComposable
    fun resolveTypography(typographyValue: PropertyValue.TypographyValue?): TextStyle {
        return when (typographyValue) {
            is PropertyValue.TypographyValue.Token -> resolveTypographyToken(typographyValue.token)
            null -> MaterialTheme.typography.bodyMedium
        }
    }

    @Composable
    @ReadOnlyComposable
    fun resolveTypographyToken(token: M3TypographyToken): TextStyle {
        val typo = MaterialTheme.typography
        return when (token) {
            M3TypographyToken.DisplayLarge -> typo.displayLarge
            M3TypographyToken.DisplayMedium -> typo.displayMedium
            M3TypographyToken.DisplaySmall -> typo.displaySmall
            M3TypographyToken.HeadlineLarge -> typo.headlineLarge
            M3TypographyToken.HeadlineMedium -> typo.headlineMedium
            M3TypographyToken.HeadlineSmall -> typo.headlineSmall
            M3TypographyToken.TitleLarge -> typo.titleLarge
            M3TypographyToken.TitleMedium -> typo.titleMedium
            M3TypographyToken.TitleSmall -> typo.titleSmall
            M3TypographyToken.BodyLarge -> typo.bodyLarge
            M3TypographyToken.BodyMedium -> typo.bodyMedium
            M3TypographyToken.BodySmall -> typo.bodySmall
            M3TypographyToken.LabelLarge -> typo.labelLarge
            M3TypographyToken.LabelMedium -> typo.labelMedium
            M3TypographyToken.LabelSmall -> typo.labelSmall
        }
    }

    @Composable
    @ReadOnlyComposable
    fun resolveShape(shapeValue: PropertyValue.ShapeValue?): Shape {
        return when (shapeValue) {
            is PropertyValue.ShapeValue.UniformDp -> RoundedCornerShape(shapeValue.cornerRadius.dp)
            is PropertyValue.ShapeValue.CornerDp -> RoundedCornerShape(
                topStart = shapeValue.topStart.dp,
                topEnd = shapeValue.topEnd.dp,
                bottomEnd = shapeValue.bottomEnd.dp,
                bottomStart = shapeValue.bottomStart.dp
            )

            is PropertyValue.ShapeValue.Token -> resolveShapeToken(shapeValue.token)
            null -> RoundedCornerShape(0.dp)
        }
    }

    @Composable
    @ReadOnlyComposable
    fun resolveShapeToken(token: M3ShapeToken): Shape {
        val shapes = MaterialTheme.shapes
        return when (token) {
            M3ShapeToken.None -> RoundedCornerShape(0.dp)
            M3ShapeToken.ExtraSmall -> shapes.extraSmall
            M3ShapeToken.Small -> shapes.small
            M3ShapeToken.Medium -> shapes.medium
            M3ShapeToken.Large -> shapes.large
            M3ShapeToken.ExtraLarge -> shapes.extraLarge
            M3ShapeToken.Full -> RoundedCornerShape(50)
        }
    }
}