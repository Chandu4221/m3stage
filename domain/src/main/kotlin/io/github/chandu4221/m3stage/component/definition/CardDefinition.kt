package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.VariantSpec
import io.github.chandu4221.m3stage.model.CardVariant
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import io.github.chandu4221.m3stage.theme.M3ShapeToken
import io.github.chandu4221.m3stage.theme.M3TypographyToken

object CardDefinition {
    val Variant = PropertyKey.EnumKey(PropertyId("card.variant"))
    val Elevation = PropertyKey.DpKey(PropertyId("card.elevation"))
    val ContainerColor = PropertyKey.ColorKey(PropertyId("card.containerColor"))
    val Shape = PropertyKey.ShapeKey(PropertyId("card.shape"))
    val BorderColor = PropertyKey.ColorKey(PropertyId("card.borderColor"))
    val BorderWidth = PropertyKey.DpKey(PropertyId("card.borderWidth"))

    val variantSpec = VariantSpec(
        key = Variant,
        defaultVariant = CardVariant.Filled,
        entries = CardVariant.entries,
        visiblePropsFor = { variant ->
            val base = setOf(Elevation.id, ContainerColor.id, Shape.id)
            when (variant) {
                CardVariant.Elevated, CardVariant.Filled -> base
                CardVariant.Outlined -> base + setOf(BorderColor.id, BorderWidth.id)
            }
        }
    )

    fun resolveVariant(node: DesignNode): CardVariant =
        variantSpec.resolveVariant(node)

    val definition = ComponentDefinition(
        kind = ComponentKind.Card,
        category = ComponentCategory.Containment,
        isContainer = true,
        variantSpec = variantSpec,
        properties = listOf(
            PropertySpec.EnumDropdown(
                key = Variant,
                displayName = "Variant",
                options = CardVariant.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(CardVariant.Filled.name)
            ),
            PropertySpec.DpSlider(
                key = Elevation,
                displayName = "Elevation (dp)",
                defaultValue = PropertyValue.DpValue(1f),
                max = 12f
            ),
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)
            ),
            PropertySpec.ShapePicker(
                key = Shape,
                displayName = "Shape",
                defaultValue = PropertyValue.ShapeValue.Token(M3ShapeToken.Medium)
            ),
            PropertySpec.ColorPicker(
                key = BorderColor,
                displayName = "Border Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)
            ),
            PropertySpec.DpSlider(
                key = BorderWidth,
                displayName = "Border Width (dp)",
                defaultValue = PropertyValue.DpValue(1f),
                min = 0.5f,
                max = 8f
            )
        ),
        defaultSubtreeFactory = { self, idGen ->
            val headlineNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("Headline"))
                    put(TextDefinition.Typography.id, PropertyValue.TypographyValue.Token(M3TypographyToken.TitleMedium))
                }
            )
            val supportingTextNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Text,
                props = TextDefinition.definition.createDefaultProps().toMutableMap().apply {
                    put(TextDefinition.TextContent.id, PropertyValue.StringValue("Supporting text"))
                    put(TextDefinition.Typography.id, PropertyValue.TypographyValue.Token(M3TypographyToken.BodyMedium))
                }
            )
            val columnNode = DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Column,
                props = ColumnDefinition.definition.createDefaultProps(),
                children = listOf(headlineNode, supportingTextNode)
            )
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Card,
                props = self.createDefaultProps(),
                children = listOf(columnNode)
            )
        }
    )
}