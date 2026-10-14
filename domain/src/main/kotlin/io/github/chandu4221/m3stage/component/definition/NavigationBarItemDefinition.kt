package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object NavigationBarItemDefinition {
    val Selected = PropertyKey.BooleanKey(PropertyId("navigationBarItem.selected"))
    val Enabled = PropertyKey.BooleanKey(PropertyId("navigationBarItem.enabled"))
    val IndicatorColor = PropertyKey.ColorKey(PropertyId("navigationBarItem.indicatorColor"))

    val IconSlot = SlotDefinition(
        id = SlotId("icon"),
        displayName = "Icon",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val LabelSlot = SlotDefinition(
        id = SlotId("label"),
        displayName = "Label",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Text)
    )

    internal fun createItem(
        idGen: IdGenerator,
        label: String,
        iconName: String,
        selected: Boolean = false
    ): DesignNode {
        val iconNode = DesignNode(
            id = idGen.nextNodeId(),
            kind = ComponentKind.Icon,
            props = IconDefinition.definition.createDefaultProps().toMutableMap().apply {
                put(IconDefinition.IconName.id, PropertyValue.StringValue(iconName))
            }
        )

        val labelNode = DesignNode(
            id = idGen.nextNodeId(),
            kind = ComponentKind.Text,
            props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                put(TextDefinition.TextContent.id, PropertyValue.StringValue(label))
            }
        )

        return DesignNode(
            id = idGen.nextNodeId(),
            kind = ComponentKind.NavigationBarItem,
            props = definition.createDefaultProps().toMutableMap().apply {
                put(Selected.id, PropertyValue.BooleanValue(selected))
            },
            slots = mapOf(
                IconSlot.id to listOf(iconNode),
                LabelSlot.id to listOf(labelNode)
            )
        )
    }

    val definition = ComponentDefinition(
        kind = ComponentKind.NavigationBarItem,
        category = ComponentCategory.Navigation,
        isContainer = true,
        slotOnly = true,
        showInPalette = false,
        properties = listOf(
            PropertySpec.Switch(
                key = Selected,
                displayName = "Selected",
                defaultValue = PropertyValue.BooleanValue(false)
            ),
            PropertySpec.Switch(
                key = Enabled,
                displayName = "Enabled",
                defaultValue = PropertyValue.BooleanValue(true)
            ),
            PropertySpec.ColorPicker(
                key = IndicatorColor,
                displayName = "Indicator Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.SecondaryContainer)
            )
        ),
        slots = listOf(IconSlot, LabelSlot),
        allowedChildren = emptySet(),
        defaultSubtreeFactory = { _, idGen ->
            createItem(idGen, label = "Item", iconName = "Star", selected = false)
        }
    )
}
