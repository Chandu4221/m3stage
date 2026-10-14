package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object NavigationBarDefinition {
    val ContainerColor = PropertyKey.ColorKey(PropertyId("navigationBar.containerColor"))

    val ItemsSlot = SlotDefinition(
        id = SlotId("items"),
        displayName = "Items",
        isRequired = true,
        maxChildren = 5,
        allowedKinds = setOf(ComponentKind.NavigationBarItem)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.NavigationBar,
        category = ComponentCategory.Navigation,
        isContainer = true,
        properties = listOf(
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.SurfaceContainer)
            )
        ),
        slots = listOf(ItemsSlot),
        allowedChildren = setOf(ComponentKind.NavigationBarItem),
        slotOnly = true,
        defaultSubtreeFactory = { self, idGen ->
            val homeItem = NavigationBarItemDefinition.createItem(idGen, "Home", "Home", selected = true)
            val searchItem = NavigationBarItemDefinition.createItem(idGen, "Search", "Search", selected = false)
            val settingsItem = NavigationBarItemDefinition.createItem(idGen, "Settings", "Settings", selected = false)
            val profileItem = NavigationBarItemDefinition.createItem(idGen, "Profile", "Person", selected = false)

            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.NavigationBar,
                props = self.createDefaultProps(),
                children = emptyList(),
                slots = mapOf(
                    ItemsSlot.id to listOf(homeItem, searchItem, settingsItem, profileItem)
                )
            )
        }
    )
}