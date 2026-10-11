package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotDefinition
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object IconButtonDefinition {
    val Enabled = PropertyKey.BooleanKey(PropertyId("iconButton.enabled"))
    val ContentColor = PropertyKey.ColorKey(PropertyId("iconButton.contentColor"))

    val IconSlot = SlotDefinition(
        id = SlotId("icon"),
        displayName = "Icon",
        isRequired = true,
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.IconButton,
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
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnSurfaceVariant)
            )
        ),
        slots = listOf(IconSlot),
        allowedChildren = setOf(ComponentKind.Icon),
        defaultSubtreeFactory = { self, idGen ->
            val iconNode = IconDefinition.definition.createDefaultSubtree(idGen)
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.IconButton,
                props = self.createDefaultProps(),
                children = listOf(iconNode),
                slots = mapOf(IconSlot.id to listOf(iconNode))
            )
        }
    )
}