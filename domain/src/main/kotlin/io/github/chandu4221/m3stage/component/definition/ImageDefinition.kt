package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue

object ImageDefinition {
    val ContentDescription = PropertyKey.StringKey(PropertyId("image.contentDescription"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Image,
        category = ComponentCategory.Communication,
        properties = listOf(
            PropertySpec.Text(
                key = ContentDescription,
                displayName = "Content Description",
                defaultValue = PropertyValue.StringValue("Image")
            )
        )
    )
}