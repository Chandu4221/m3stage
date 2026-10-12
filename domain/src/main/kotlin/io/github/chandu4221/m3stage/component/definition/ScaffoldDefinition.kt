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

object ScaffoldDefinition {
    val ContainerColor = PropertyKey.ColorKey(PropertyId("scaffold.containerColor"))

    // Official M3 Anatomy Slots
    val TopBarSlot = SlotDefinition(
        id = SlotId("topBar"),
        displayName = "Top App Bar",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.TopAppBar)
    )

    val BottomBarSlot = SlotDefinition(
        id = SlotId("bottomBar"),
        displayName = "Bottom Bar",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.NavigationBar, ComponentKind.Row, ComponentKind.Box)
    )

    val FloatingActionButtonSlot = SlotDefinition(
        id = SlotId("floatingActionButton"),
        displayName = "Floating Action Button",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.FloatingActionButton, ComponentKind.Button, ComponentKind.Icon)
    )

    val ContentSlot = SlotDefinition(
        id = SlotId("content"),
        displayName = "Content",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Column, ComponentKind.Row, ComponentKind.Box, ComponentKind.Card)
    )
    val definition = ComponentDefinition(
        kind = ComponentKind.Scaffold,
        category = ComponentCategory.Containment,
        isContainer = true,
        properties = listOf(
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Background)
            )
        ),
        slots = listOf(TopBarSlot, BottomBarSlot, FloatingActionButtonSlot, ContentSlot),
        allowedChildren = setOf(
            ComponentKind.TopAppBar,
            ComponentKind.NavigationBar,
            ComponentKind.FloatingActionButton,
            ComponentKind.Column,
            ComponentKind.Row,
            ComponentKind.Box,
            ComponentKind.Card,
            ComponentKind.Button
        ),
        defaultSubtreeFactory = { self, idGen ->
            val topBarNode = TopAppBarDefinition.definition.createDefaultSubtree(idGen)
            val contentColumnNode = ColumnDefinition.definition.createDefaultSubtree(idGen)

            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Scaffold,
                props = self.createDefaultProps(),
                children = emptyList(),
                slots = mapOf(
                    TopBarSlot.id to listOf(topBarNode),
                    ContentSlot.id to listOf(contentColumnNode)
                )
            )
        }
    )
}