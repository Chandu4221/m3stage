package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object BadgeDefinition {
    val ContainerColor = PropertyKey.ColorKey(PropertyId("badge.containerColor"))
    val ContentColor = PropertyKey.ColorKey(PropertyId("badge.contentColor"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Badge,
        category = ComponentCategory.Communication,
        isContainer = true,
        properties = listOf(
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Error)
            ),
            PropertySpec.ColorPicker(
                key = ContentColor,
                displayName = "Content Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnError)
            )
        ),
        allowedChildren = setOf(ComponentKind.Text),
        defaultSubtreeFactory = { self, idGen ->
            val textNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("1"))
                    put(TextDefinition.Color.id, PropertyValue.ColorValue.Token(M3ColorToken.Inherit))
                }
            )
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Badge,
                props = self.createDefaultProps(),
                children = listOf(textNode)
            )
        }
    )
}