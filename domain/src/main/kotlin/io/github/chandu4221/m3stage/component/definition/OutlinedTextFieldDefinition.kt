package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*

object OutlinedTextFieldDefinition {
    val Value = PropertyKey.StringKey(PropertyId("outlinedTextField.value"))
    val Label = PropertyKey.StringKey(PropertyId("outlinedTextField.label"))
    val Placeholder = PropertyKey.StringKey(PropertyId("outlinedTextField.placeholder"))
    val SingleLine = PropertyKey.BooleanKey(PropertyId("outlinedTextField.singleLine"))

    val definition = ComponentDefinition(
        kind = ComponentKind.OutlinedTextField,
        category = ComponentCategory.TextInputs,
        isContainer = true,
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
        slots = listOf(
            TextFieldDefinition.LeadingIconSlot,
            TextFieldDefinition.TrailingIconSlot,
            TextFieldDefinition.SupportingTextSlot
        ),
        allowedChildren = setOf(ComponentKind.Icon, ComponentKind.Text),
        defaultSubtreeFactory = { self, idGen ->
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.OutlinedTextField,
                props = self.createDefaultProps().toMutableMap().apply {
                    put(Value.id, PropertyValue.StringValue("Value"))
                }
            )
        }
    )
}