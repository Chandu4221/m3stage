package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.*

object ColumnDefinition {
    val Spacing = PropertyKey.DpKey(PropertyId("column.spacing"))
    val HorizontalAlignment = PropertyKey.EnumKey(PropertyId("column.horizontalAlignment"))
    val VerticalArrangement = PropertyKey.EnumKey(PropertyId("column.verticalArrangement"))

    val definition = ComponentDefinition(
        kind = ComponentKind.Column,
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
                key = HorizontalAlignment,
                displayName = "Horizontal Alignment",
                options = HorizontalAlignmentOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(HorizontalAlignmentOption.Start.name)
            ),
            PropertySpec.EnumDropdown(
                key = VerticalArrangement,
                displayName = "Vertical Arrangement",
                options = VerticalArrangementOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(VerticalArrangementOption.Top.name)
            )
        )
    )
}