package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object TextButtonDefinition {
    val Enabled = PropertyKey.BooleanKey(PropertyId("textButton.enabled"))
    val ContentColor = PropertyKey.ColorKey(PropertyId("textButton.contentColor"))

    val LabelSlot = SlotDefinition(
        id = SlotId("label"),
        displayName = "Label Text",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Text)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.TextButton,
        category = ComponentCategory.Actions,
        isContainer = true,
        properties = listOf(
            PropertySpec.Switch(
                key = Enabled,
                displayName = "Enabled",
                defaultValue = PropertyValue.BooleanValue(true)
            ),
            PropertySpec.ColorPicker(
                key = ContentColor,
                displayName = "Content Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Primary)
            )
        ),
        slots = listOf(ButtonDefinition.LeadingIconSlot, LabelSlot, ButtonDefinition.TrailingIconSlot),
        allowedChildren = setOf(ComponentKind.Text, ComponentKind.Icon),
        defaultSubtreeFactory = { self, idGen ->
            val labelTextNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("Text Button"))
                }
            )
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.TextButton,
                props = self.createDefaultProps(),
                children = listOf(labelTextNode),
                slots = mapOf(LabelSlot.id to listOf(labelTextNode))
            )
        }
    )
}