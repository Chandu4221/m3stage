package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.ContentAlignmentOption
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue

object BoxDefinition {
    val ContentAlignment = PropertyKey.EnumKey(PropertyId("box.contentAlignment"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Box,
        category = ComponentCategory.Layouts,
        isContainer = true,
        properties = listOf(
            PropertySpec.EnumDropdown(
                key = ContentAlignment,
                displayName = "Content Alignment",
                options = ContentAlignmentOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(ContentAlignmentOption.Center.name)
            )
        )
    )
}