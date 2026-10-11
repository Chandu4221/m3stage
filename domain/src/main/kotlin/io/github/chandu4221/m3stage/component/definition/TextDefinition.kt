package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

object TextDefinition {
    val TextContent = PropertyKey.StringKey(PropertyId("text.content"))
    val Typography = PropertyKey.TypographyKey(PropertyId("text.typography"))
    val Color = PropertyKey.ColorKey(PropertyId("text.color"))
    val Overflow = PropertyKey.EnumKey(PropertyId("text.overflow"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Text,
        category = ComponentCategory.Communication,
        properties = listOf(
            PropertySpec.Text(
                key = TextContent,
                displayName = "Text Content",
                defaultValue = PropertyValue.StringValue("Sample Text")
            ),
            PropertySpec.TypographyPicker(
                key = Typography,
                displayName = "Typography",
                defaultValue = PropertyValue.TypographyValue.Token(M3TypographyToken.BodyMedium)
            ),
            PropertySpec.ColorPicker(
                key = Color,
                displayName = "Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnSurface)
            ),
            PropertySpec.EnumDropdown(
                key = Overflow,
                displayName = "Text Overflow",
                options = TextOverflowOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(TextOverflowOption.Clip.name)
            )
        )
    )
}