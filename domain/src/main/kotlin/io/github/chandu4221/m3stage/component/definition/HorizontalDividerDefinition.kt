package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object HorizontalDividerDefinition {
    val Thickness = PropertyKey.DpKey(PropertyId("horizontalDivider.thickness"))
    val Color = PropertyKey.ColorKey(PropertyId("horizontalDivider.color"))

    val definition = ComponentDefinition(
        kind = ComponentKind.HorizontalDivider,
        category = ComponentCategory.Containment,
        isContainer = false,
        properties = listOf(
            PropertySpec.DpSlider(
                key = Thickness,
                displayName = "Thickness (dp)",
                defaultValue = PropertyValue.DpValue(1f),
                min = 1f,
                max = 8f
            ),
            PropertySpec.ColorPicker(
                key = Color,
                displayName = "Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OutlineVariant)
            )
        )
    )
}