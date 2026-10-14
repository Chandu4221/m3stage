package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*

object CheckboxDefinition {
    val Checked = PropertyKey.BooleanKey(PropertyId("checkbox.checked"))
    val Enabled = PropertyKey.BooleanKey(PropertyId("checkbox.enabled"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Checkbox,
        category = ComponentCategory.Selection,
        isContainer = false,
        properties = listOf(
            PropertySpec.Switch(
                key = Checked,
                displayName = "Checked",
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
                kind = ComponentKind.Checkbox,
                props = self.createDefaultProps()
            )
        }
    )
}