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

object TopAppBarDefinition {
    val Title = PropertyKey.StringKey(PropertyId("topAppBar.title"))
    val ContainerColor = PropertyKey.ColorKey(PropertyId("topAppBar.containerColor"))
    val TitleContentColor = PropertyKey.ColorKey(PropertyId("topAppBar.titleContentColor"))

    // Official M3 Anatomy Slots
    val NavigationIconSlot = SlotDefinition(
        id = SlotId("navigationIcon"),
        displayName = "Navigation Icon",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon, ComponentKind.Button)
    )

    val TitleSlot = SlotDefinition(
        id = SlotId("title"),
        displayName = "Title",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Text)
    )

    val ActionsSlot = SlotDefinition(
        id = SlotId("actions"),
        displayName = "Action Items",
        allowedKinds = setOf(ComponentKind.Icon, ComponentKind.Button)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.TopAppBar,
        category = ComponentCategory.Navigation,
        isContainer = true,
        properties = listOf(
            PropertySpec.Text(
                key = Title,
                displayName = "Title",
                defaultValue = PropertyValue.StringValue("Title")
            ),
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Surface)
            ),
            PropertySpec.ColorPicker(
                key = TitleContentColor,
                displayName = "Title Content Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnSurface)
            )
        ),
        slots = listOf(NavigationIconSlot, TitleSlot, ActionsSlot),
        allowedChildren = setOf(ComponentKind.Text, ComponentKind.Icon, ComponentKind.Button),
        slotOnly = true,
        defaultSubtreeFactory = { self, idGen ->
            val titleNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("Title"))
                }
            )
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.TopAppBar,
                props = self.createDefaultProps(),
                children = emptyList(),
                slots = mapOf(TitleSlot.id to listOf(titleNode))
            )
        }
    )
}