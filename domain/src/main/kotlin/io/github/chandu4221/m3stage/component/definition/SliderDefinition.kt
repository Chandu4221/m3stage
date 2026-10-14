package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*

object SliderDefinition {
    val Value = PropertyKey.DpKey(PropertyId("slider.value")) // Stores float value
    val Enabled = PropertyKey.BooleanKey(PropertyId("slider.enabled"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Slider,
        category = ComponentCategory.Selection,
        isContainer = false,
        properties = listOf(
            PropertySpec.DpSlider(
                key = Value,
                displayName = "Value (0-100)",
                defaultValue = PropertyValue.DpValue(50f),
                min = 0f,
                max = 100f
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
                kind = ComponentKind.Slider,
                props = self.createDefaultProps()
            )
        }
    )
}