package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken

object CardDefinition {
    val Elevation = PropertyKey.DpKey(PropertyId("card.elevation"))
    val ContainerColor = PropertyKey.ColorKey(PropertyId("card.containerColor"))
    val Shape = PropertyKey.ShapeKey(PropertyId("card.shape"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Card,
        category = ComponentCategory.Containment,
        isContainer = true,
        properties = listOf(
            PropertySpec.DpSlider(
                key = Elevation,
                displayName = "Elevation (dp)",
                defaultValue = PropertyValue.DpValue(1f),
                max = 12f
            ),
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Surface)
            ),
            PropertySpec.ShapePicker(
                key = Shape,
                displayName = "Shape",
                defaultValue = PropertyValue.ShapeValue.Token(M3ShapeToken.Medium)
            )
        )
    )
}