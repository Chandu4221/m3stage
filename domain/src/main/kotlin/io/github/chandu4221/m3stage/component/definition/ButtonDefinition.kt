package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken

object ButtonDefinition {
    // Properties
    val Enabled = PropertyKey.BooleanKey(PropertyId("button.enabled"))
    val ContainerColor = PropertyKey.ColorKey(PropertyId("button.containerColor"))
    val ContentColor = PropertyKey.ColorKey(PropertyId("button.contentColor"))

    // Official M3 Anatomy Slots
    val LeadingIconSlot = SlotDefinition(
        id = SlotId("leadingIcon"),
        displayName = "Leading Icon",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val LabelSlot = SlotDefinition(
        id = SlotId("label"),
        displayName = "Label Text",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Text)
    )

    val TrailingIconSlot = SlotDefinition(
        id = SlotId("trailingIcon"),
        displayName = "Trailing Icon",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.Button,
        category = ComponentCategory.Actions,
        isContainer = true,
        properties = listOf(
            PropertySpec.Switch(
                key = Enabled,
                displayName = "Enabled",
                defaultValue = PropertyValue.BooleanValue(true)
            ),
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Primary)
            ),
            PropertySpec.ColorPicker(
                key = ContentColor,
                displayName = "Content Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnPrimary)
            )
        ),
        slots = listOf(LeadingIconSlot, LabelSlot, TrailingIconSlot),
        allowedChildren = setOf(ComponentKind.Text, ComponentKind.Icon),
        defaultSubtreeFactory = { self, idGen ->
            val labelTextNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("Button"))
                    put(TextDefinition.Color.id, PropertyValue.ColorValue.Token(M3ColorToken.Inherit))
                }
            )
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Button,
                props = self.createDefaultProps(),
                children = emptyList(),
                slots = mapOf(LabelSlot.id to listOf(labelTextNode))
            )
        }
    )
}