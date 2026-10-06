package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

object ComponentCatalog {

    // =========================================================================
    // METAMODEL PROPERTY KEYS
    // =========================================================================

    object TextProps {
        val TextContent = PropertyKey.StringKey(PropertyId("text.content"))
        val Typography = PropertyKey.TypographyKey(PropertyId("text.typography"))
        val Color = PropertyKey.ColorKey(PropertyId("text.color"))
        val Overflow = PropertyKey.EnumKey(PropertyId("text.overflow"))
    }

    object ButtonProps {
        val Enabled = PropertyKey.BooleanKey(PropertyId("button.enabled"))
        val ContainerColor = PropertyKey.ColorKey(PropertyId("button.containerColor"))
        val ContentColor = PropertyKey.ColorKey(PropertyId("button.contentColor"))
    }

    object ColumnProps {
        val Spacing = PropertyKey.DpKey(PropertyId("column.spacing"))
        val HorizontalAlignment = PropertyKey.EnumKey(PropertyId("column.horizontalAlignment"))
        val VerticalArrangement = PropertyKey.EnumKey(PropertyId("column.verticalArrangement"))
    }

    object RowProps {
        val Spacing = PropertyKey.DpKey(PropertyId("row.spacing"))
        val HorizontalArrangement = PropertyKey.EnumKey(PropertyId("row.horizontalArrangement"))
        val VerticalAlignment = PropertyKey.EnumKey(PropertyId("row.verticalAlignment"))
    }

    object BoxProps {
        val ContentAlignment = PropertyKey.EnumKey(PropertyId("box.contentAlignment"))
    }

    object CardProps {
        val Elevation = PropertyKey.DpKey(PropertyId("card.elevation"))
        val ContainerColor = PropertyKey.ColorKey(PropertyId("card.containerColor"))
        val Shape = PropertyKey.ShapeKey(PropertyId("card.shape"))
    }

    // =========================================================================
    // COMPONENT DEFINITIONS
    // =========================================================================

    val Text = ComponentDefinition(
        kind = ComponentKind.Text,
        descriptors = listOf(
            PropertyDescriptor.Text(
                key = TextProps.TextContent,
                displayName = "Text Content",
                defaultValue = PropertyValue.StringValue("Sample Text")
            ),
            PropertyDescriptor.TypographyPicker(
                key = TextProps.Typography,
                displayName = "Typography",
                defaultValue = PropertyValue.TypographyValue.Token(M3TypographyToken.BodyMedium)
            ),
            PropertyDescriptor.ColorPicker(
                key = TextProps.Color,
                displayName = "Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnSurface)
            ),
            PropertyDescriptor.EnumDropdown(
                key = TextProps.Overflow,
                displayName = "Text Overflow",
                options = TextOverflowOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(TextOverflowOption.Clip.name)
            )
        )
    )

    val Button = ComponentDefinition(
        kind = ComponentKind.Button,
        descriptors = listOf(
            PropertyDescriptor.Switch(
                key = ButtonProps.Enabled,
                displayName = "Enabled",
                defaultValue = PropertyValue.BooleanValue(true)
            ),
            PropertyDescriptor.ColorPicker(
                key = ButtonProps.ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Primary)
            ),
            PropertyDescriptor.ColorPicker(
                key = ButtonProps.ContentColor,
                displayName = "Content Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.OnPrimary)
            )
        ),
        // Atomic Design: Button is a molecule that only accepts Text and Icon atoms
        allowedChildren = setOf(ComponentKind.Text, ComponentKind.Icon)
    )

    val Column = ComponentDefinition(
        kind = ComponentKind.Column,
        descriptors = listOf(
            PropertyDescriptor.DpSlider(
                key = ColumnProps.Spacing,
                displayName = "Item Spacing (dp)",
                defaultValue = PropertyValue.DpValue(0f),
                max = 64f
            ),
            PropertyDescriptor.EnumDropdown(
                key = ColumnProps.HorizontalAlignment,
                displayName = "Horizontal Alignment",
                options = HorizontalAlignmentOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(HorizontalAlignmentOption.Start.name)
            ),
            PropertyDescriptor.EnumDropdown(
                key = ColumnProps.VerticalArrangement,
                displayName = "Vertical Arrangement",
                options = VerticalArrangementOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(VerticalArrangementOption.Top.name)
            )
        )
    )

    val Row = ComponentDefinition(
        kind = ComponentKind.Row,
        descriptors = listOf(
            PropertyDescriptor.DpSlider(
                key = RowProps.Spacing,
                displayName = "Item Spacing (dp)",
                defaultValue = PropertyValue.DpValue(0f),
                max = 64f
            ),
            PropertyDescriptor.EnumDropdown(
                key = RowProps.HorizontalArrangement,
                displayName = "Horizontal Arrangement",
                options = HorizontalArrangementOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(HorizontalArrangementOption.Start.name)
            ),
            PropertyDescriptor.EnumDropdown(
                key = RowProps.VerticalAlignment,
                displayName = "Vertical Alignment",
                options = VerticalAlignmentOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(VerticalAlignmentOption.CenterVertically.name)
            )
        )
    )

    val Box = ComponentDefinition(
        kind = ComponentKind.Box,
        descriptors = listOf(
            PropertyDescriptor.EnumDropdown(
                key = BoxProps.ContentAlignment,
                displayName = "Content Alignment",
                options = ContentAlignmentOption.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(ContentAlignmentOption.Center.name)
            )
        )
    )

    val Card = ComponentDefinition(
        kind = ComponentKind.Card,
        descriptors = listOf(
            PropertyDescriptor.DpSlider(
                key = CardProps.Elevation,
                displayName = "Elevation (dp)",
                defaultValue = PropertyValue.DpValue(1f),
                max = 12f
            ),
            PropertyDescriptor.ColorPicker(
                key = CardProps.ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Surface)
            ),
            PropertyDescriptor.ShapePicker(
                key = CardProps.Shape,
                displayName = "Shape",
                defaultValue = PropertyValue.ShapeValue.Token(M3ShapeToken.Medium)
            )
        )
    )

    // =========================================================================
    // REGISTRY & LOOKUPS
    // =========================================================================

    val all: List<ComponentDefinition> = listOf(Text, Button, Column, Row, Box, Card)

    private val byKind: Map<ComponentKind, ComponentDefinition> = all.associateBy { it.kind }

    /**
     * O(1) Non-nullable lookup by ComponentKind.
     */
    operator fun get(kind: ComponentKind): ComponentDefinition =
        byKind[kind] ?: error("No ComponentDefinition registered for $kind in ComponentCatalog")

    /**
     * Nullable lookup if probing for optional support.
     */
    fun find(kind: ComponentKind): ComponentDefinition? = byKind[kind]

    /**
     * Grouped by UI palette category for easy palette rendering.
     */
    val groupedByCategory: Map<ComponentCategory, List<ComponentDefinition>> =
        all.groupBy { it.category }
}