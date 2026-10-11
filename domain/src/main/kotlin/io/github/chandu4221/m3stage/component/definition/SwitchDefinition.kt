package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.*

object SwitchDefinition {
    val Checked = PropertyKey.BooleanKey(PropertyId("switch.checked"))
    val Enabled = PropertyKey.BooleanKey(PropertyId("switch.enabled"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Switch,
        category = ComponentCategory.Selection,
        isContainer = false,
        properties = listOf(
            PropertySpec.Switch(
                key = Checked,
                displayName = "Checked",
                defaultValue = PropertyValue.BooleanValue(true)
            ),
            PropertySpec.Switch(
                key = Enabled,
                displayName = "Enabled",
                defaultValue = PropertyValue.BooleanValue(true)
            )
        )
    )
}