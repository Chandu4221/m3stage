package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken

object IconDefinition {
    val IconName = PropertyKey.StringKey(PropertyId("icon.name"))
    val Tint = PropertyKey.ColorKey(PropertyId("icon.tint"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Icon,
        category = ComponentCategory.Communication,
        properties = listOf(
            PropertySpec.Text(
                key = IconName,
                displayName = "Icon Name",
                defaultValue = PropertyValue.StringValue("Star")
            ),
            PropertySpec.ColorPicker(
                key = Tint,
                displayName = "Tint Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Primary)
            )
        ),
        defaultSubtreeFactory = { self, idGen ->
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Icon,
                props = self.createDefaultProps().toMutableMap().apply {
                    put(IconName.id, PropertyValue.StringValue("Add"))
                }
            )
        }
    )
}