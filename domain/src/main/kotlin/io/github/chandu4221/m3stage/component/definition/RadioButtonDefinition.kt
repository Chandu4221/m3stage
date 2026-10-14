package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*

object RadioButtonDefinition {
    val Selected = PropertyKey.BooleanKey(PropertyId("radioButton.selected"))
    val Enabled = PropertyKey.BooleanKey(PropertyId("radioButton.enabled"))

    val definition = ComponentDefinition(
        kind = ComponentKind.RadioButton,
        category = ComponentCategory.Selection,
        isContainer = false,
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
            )
        ),
        defaultSubtreeFactory = { self, idGen ->
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.RadioButton,
                props = self.createDefaultProps()
            )
        }
    )
}