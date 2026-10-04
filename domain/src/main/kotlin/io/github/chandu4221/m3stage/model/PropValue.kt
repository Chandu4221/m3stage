package io.github.chandu4221.m3stage.model

sealed interface PropVal {
    data class Str(val value: String) : PropVal
    data class Bool(val value: Boolean) : PropVal
    data class Num(val value: Double) : PropVal
    data class DpVal(val value: Float) : PropVal
    data class SpVal(val value: Float) : PropVal
    data class EnumVal(val name: String) : PropVal // Added for Alignment, Arrangement, Overflow, etc.

    sealed interface ColorVal : PropVal {
        data class Token(val token: M3ColorToken) : ColorVal
        data class Custom(val argb: Long) : ColorVal
    }

    sealed interface TypographyVal : PropVal {
        data class Token(val token: M3TypographyToken) : TypographyVal
    }

    sealed interface ShapeVal : PropVal {
        data class Token(val token: M3ShapeToken) : ShapeVal
        data class UniformDp(val cornerRadius: Float) : ShapeVal
        data class CornerDp(
            val topStart: Float = 0f,
            val topEnd: Float = 0f,
            val bottomEnd: Float = 0f,
            val bottomStart: Float = 0f
        ) : ShapeVal
    }
}

// Expanded to match actual Material 3 ColorScheme properties
enum class M3ColorToken {
    Primary, OnPrimary, PrimaryContainer, OnPrimaryContainer,
    Secondary, OnSecondary, SecondaryContainer, OnSecondaryContainer,
    Tertiary, OnTertiary, TertiaryContainer, OnTertiaryContainer,
    Surface, OnSurface, SurfaceVariant, OnSurfaceVariant,
    Background, OnBackground,
    Error, OnError, ErrorContainer, OnErrorContainer,
    Outline, OutlineVariant
}

// Expanded to all 15 M3 Typography tokens
enum class M3TypographyToken {
    DisplayLarge, DisplayMedium, DisplaySmall,
    HeadlineLarge, HeadlineMedium, HeadlineSmall,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelMedium, LabelSmall
}

enum class M3ShapeToken { None, ExtraSmall, Small, Medium, Large, ExtraLarge, Full }