package io.github.chandu4221.m3stage.model

object ComponentCatalog {

    // --- METAMODELS (PropKeys) ---
    object TextProps {
        val TextContent = PropKey.StrKey(PropId("text.content"))
        val Typography = PropKey.TypographyKey(PropId("text.typography"))
        val Color = PropKey.ColorKey(PropId("text.color"))
        val MaxLines = PropKey.NumKey(PropId("text.maxLines"))
        val Overflow = PropKey.EnumKey(PropId("text.overflow"))
    }

    object ButtonProps {
        val Enabled = PropKey.BoolKey(PropId("button.enabled"))
        val TextContent = PropKey.StrKey(PropId("button.text"))
        val ContainerColor = PropKey.ColorKey(PropId("button.containerColor"))
        val ContentColor = PropKey.ColorKey(PropId("button.contentColor"))
    }

    object LayoutProps {
        val Spacing = PropKey.DpKey(PropId("layout.spacing"))
        val HorizontalAlignment = PropKey.EnumKey(PropId("layout.horizontalAlignment"))
        val VerticalArrangement = PropKey.EnumKey(PropId("layout.verticalArrangement"))
        val Padding = PropKey.DpKey(PropId("layout.padding"))
    }

    // ... (Keep TextProps, ButtonProps, LayoutProps, CardProps as they are) ...

    object RowProps {
        val Spacing = PropKey.DpKey(PropId("row.spacing"))
        val HorizontalArrangement = PropKey.EnumKey(PropId("row.horizontalArrangement"))
        val VerticalAlignment = PropKey.EnumKey(PropId("row.verticalAlignment"))
    }

    object BoxProps {
        val ContentAlignment = PropKey.EnumKey(PropId("box.contentAlignment"))
    }

    // ... (Keep Text, Button, Column, Card definitions as they are) ...
    // --- Add these Definitions ---
    val Row = ComponentDefinition(
        type = ComponentTypes.Row,
        category = ComponentCategory.LAYOUTS,
        displayName = "Row",
        descriptors = listOf(
            PropDescriptor.DpSlider(RowProps.Spacing, "Item Spacing (dp)", PropVal.DpVal(0f), max = 64f),
            PropDescriptor.EnumDropdown(
                RowProps.HorizontalArrangement, "Horizontal Arrangement",
                listOf("Start", "Center", "End", "SpaceBetween", "SpaceAround"),
                PropVal.EnumVal("Start")
            ),
            PropDescriptor.EnumDropdown(
                RowProps.VerticalAlignment, "Vertical Alignment",
                listOf("Top", "CenterVertically", "Bottom"),
                PropVal.EnumVal("CenterVertically")
            )
        ),
        isContainer = true
    )

    val Box = ComponentDefinition(
        type = ComponentTypes.Box,
        category = ComponentCategory.LAYOUTS,
        displayName = "Box",
        descriptors = listOf(
            PropDescriptor.EnumDropdown(
                BoxProps.ContentAlignment, "Content Alignment",
                listOf(
                    "TopStart",
                    "TopCenter",
                    "TopEnd",
                    "CenterStart",
                    "Center",
                    "CenterEnd",
                    "BottomStart",
                    "BottomCenter",
                    "BottomEnd"
                ),
                PropVal.EnumVal("Center")
            )
        ),
        isContainer = true
    )

    object CardProps {
        val Elevation = PropKey.DpKey(PropId("card.elevation"))
        val ContainerColor = PropKey.ColorKey(PropId("card.containerColor"))
        val Shape = PropKey.ShapeKey(PropId("card.shape"))
    }

    // --- DEFINITIONS ---
    val Text = ComponentDefinition(
        type = ComponentTypes.Text,
        category = ComponentCategory.DISPLAY,
        displayName = "Text",
        descriptors = listOf(
            PropDescriptor.Text(TextProps.TextContent, "Text Content", PropVal.Str("Sample Text")),
            PropDescriptor.TypographyPicker(
                TextProps.Typography,
                "Typography",
                PropVal.TypographyVal.Token(M3TypographyToken.BodyMedium)
            ),
            PropDescriptor.ColorPicker(TextProps.Color, "Color", PropVal.ColorVal.Token(M3ColorToken.OnSurface)),
            PropDescriptor.EnumDropdown(
                TextProps.Overflow, "Text Overflow",
                listOf("Clip", "Ellipsis", "Visible"),
                PropVal.EnumVal("Clip")
            )
        ),
        isContainer = false
    )

    val Button = ComponentDefinition(
        type = ComponentTypes.Button,
        category = ComponentCategory.INPUTS,
        displayName = "Button",
        descriptors = listOf(
            PropDescriptor.Switch(ButtonProps.Enabled, "Enabled", PropVal.Bool(true)),
            PropDescriptor.Text(ButtonProps.TextContent, "Button Text", PropVal.Str("Button")),
            PropDescriptor.ColorPicker(
                ButtonProps.ContainerColor,
                "Container Color",
                PropVal.ColorVal.Token(M3ColorToken.Primary)
            ),
            PropDescriptor.ColorPicker(
                ButtonProps.ContentColor,
                "Content Color",
                PropVal.ColorVal.Token(M3ColorToken.OnPrimary)
            )
        ),
        defaultChildTypes = listOf(ComponentTypes.Text),
        isContainer = true
    )

    val Column = ComponentDefinition(
        type = ComponentTypes.Column,
        category = ComponentCategory.LAYOUTS,
        displayName = "Column",
        descriptors = listOf(
            PropDescriptor.DpSlider(LayoutProps.Spacing, "Item Spacing (dp)", PropVal.DpVal(0f), max = 64f),
            PropDescriptor.EnumDropdown(
                LayoutProps.HorizontalAlignment, "Horizontal Alignment",
                listOf("Start", "CenterHorizontally", "End"),
                PropVal.EnumVal("Start")
            ),
            PropDescriptor.EnumDropdown(
                LayoutProps.VerticalArrangement, "Vertical Arrangement",
                listOf("Top", "Center", "Bottom", "SpaceBetween", "SpaceAround"),
                PropVal.EnumVal("Top")
            )
        ),
        isContainer = true
    )

    val Card = ComponentDefinition(
        type = ComponentTypes.Card,
        category = ComponentCategory.SURFACES,
        displayName = "Card",
        descriptors = listOf(
            PropDescriptor.DpSlider(CardProps.Elevation, "Elevation (dp)", PropVal.DpVal(1f), max = 12f),
            PropDescriptor.ColorPicker(
                CardProps.ContainerColor,
                "Container Color",
                PropVal.ColorVal.Token(M3ColorToken.Surface)
            ),
            PropDescriptor.ShapePicker( // <-- Fixed: Use ShapePicker
                CardProps.Shape,
                "Shape",
                PropVal.ShapeVal.Token(M3ShapeToken.Medium) // <-- Fixed: Use actual ShapeVal
            )
        ),
        defaultChildTypes = listOf(ComponentTypes.Column),
        isContainer = true
    )

    // --- REGISTRY ---
    val all: List<ComponentDefinition> = listOf(Text, Button, Column, Row, Box, Card)

    fun getByType(type: ComponentType): ComponentDefinition? = all.find { it.type == type }

    val groupedByCategory: Map<ComponentCategory, List<ComponentDefinition>> =
        all.groupBy { it.category }
}