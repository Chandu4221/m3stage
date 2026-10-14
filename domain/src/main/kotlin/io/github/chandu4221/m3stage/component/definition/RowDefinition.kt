package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.*

object RowDefinition {
    val Spacing = PropertyKey.DpKey(PropertyId("row.spacing"))
    val HorizontalArrangement = PropertyKey.EnumKey(PropertyId("row.horizontalArrangement"))
    val VerticalAlignment = PropertyKey.EnumKey(PropertyId("row.verticalAlignment"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Row,
        category = ComponentCategory.Layouts,
        isContainer = true,
        properties = listOf(
            PropertySpec.DpSlider(
                key = Spacing,
                displayName = "Item Spacing (dp)",
                defaultValue = PropertyValue.DpValue(0f),
                max = 64f
            ),
            PropertySpec.EnumDropdown(
                key = HorizontalArrangement,
                displayName = "Horizontal Arrangement",
                options = HorizontalArrangementOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(HorizontalArrangementOption.Start.name)
            ),
            PropertySpec.EnumDropdown(
                key = VerticalAlignment,
                displayName = "Vertical Alignment",
                options = VerticalAlignmentOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(VerticalAlignmentOption.CenterVertically.name)
            )
        ),
        defaultSubtreeFactory = { self, idGen ->
            val textNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("Content"))
                }
            )
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Row,
                props = self.createDefaultProps(),
                children = listOf(textNode)
            )
        }
    )
}