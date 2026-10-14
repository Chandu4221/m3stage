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

object TextFieldDefinition {
    // Properties
    val Value = PropertyKey.StringKey(PropertyId("textField.value"))
    val Label = PropertyKey.StringKey(PropertyId("textField.label"))
    val Placeholder = PropertyKey.StringKey(PropertyId("textField.placeholder"))
    val SingleLine = PropertyKey.BooleanKey(PropertyId("textField.singleLine"))

    // Official M3 Anatomy Slots
    val LeadingIconSlot = SlotDefinition(
        id = SlotId("leadingIcon"),
        displayName = "Leading Icon",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val TrailingIconSlot = SlotDefinition(
        id = SlotId("trailingIcon"),
        displayName = "Trailing Icon",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Icon)
    )

    val SupportingTextSlot = SlotDefinition(
        id = SlotId("supportingText"),
        displayName = "Supporting Text",
        maxChildren = 1,
        allowedKinds = setOf(ComponentKind.Text)
    )

    val definition = ComponentDefinition(
        kind = ComponentKind.TextField,
        category = ComponentCategory.TextInputs,
        isContainer = true, // Has leading/trailing/supporting slots!
        properties = listOf(
            PropertySpec.Text(
                key = Label,
                displayName = "Label",
                defaultValue = PropertyValue.StringValue("Label")
            ),
            PropertySpec.Text(
                key = Placeholder,
                displayName = "Placeholder",
                defaultValue = PropertyValue.StringValue("Enter text...")
            ),
            PropertySpec.Text(
                key = Value,
                displayName = "Value",
                defaultValue = PropertyValue.StringValue("")
            ),
            PropertySpec.Switch(
                key = SingleLine,
                displayName = "Single Line",
                defaultValue = PropertyValue.BooleanValue(true)
            )
        ),
        slots = listOf(LeadingIconSlot, TrailingIconSlot, SupportingTextSlot),
        allowedChildren = setOf(ComponentKind.Icon, ComponentKind.Text),
        defaultSubtreeFactory = { self, idGen ->
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.TextField,
                props = self.createDefaultProps().toMutableMap().apply {
                    put(Value.id, PropertyValue.StringValue("Value"))
                }
            )
        }
    )
}