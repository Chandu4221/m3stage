package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object FloatingActionButtonDefinition {
    val ContainerColor = PropertyKey.ColorKey(PropertyId("fab.containerColor"))
    val ContentColor = PropertyKey.ColorKey(PropertyId("fab.contentColor"))

    val IconSlot = SlotDefinition(
        id = SlotId("icon"),
        displayName = "Icon",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.FloatingActionButton,
        category = ComponentCategory.Actions,
        isContainer = true,
        properties = listOf(
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)
            ),
            PropertySpec.ColorPicker(
                key = ContentColor,
                displayName = "Content Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)
            )
        ),
        slots = listOf(IconSlot),
        allowedChildren = setOf(ComponentKind.Icon, ComponentKind.Text),
        defaultSubtreeFactory = { self, idGen ->
            val iconNode = IconDefinition.definition.createDefaultSubtree(idGen).let {
                it.copy(props = it.props.toMutableMap().apply {
                    put(IconDefinition.Glyph.id, PropertyValue.EnumValue("Add"))
                    put(IconDefinition.IconName.id, PropertyValue.StringValue("Add"))
                })
            }
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.FloatingActionButton,
                props = self.createDefaultProps(),
                children = emptyList(),
                slots = mapOf(IconSlot.id to listOf(iconNode))
            )
        }
    )
}