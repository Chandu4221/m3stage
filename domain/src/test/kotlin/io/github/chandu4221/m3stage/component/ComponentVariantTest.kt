package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.component.definition.FloatingActionButtonDefinition
import io.github.chandu4221.m3stage.model.CardVariant
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.FabVariant
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComponentVariantTest {

    @Test
    fun testCardDefaultVariantIsFilled() {
        val defaultProps = CardDefinition.definition.createDefaultProps()
        val variantVal = defaultProps[CardDefinition.Variant.id] as? PropertyValue.EnumValue
        assertEquals(CardVariant.Filled.name, variantVal?.name)

        val freshNode = DesignNode(
            id = NodeId("card-1"),
            kind = ComponentKind.Card,
            props = defaultProps
        )
        assertEquals(CardVariant.Filled, CardDefinition.resolveVariant(freshNode))
    }

    @Test
    fun testFabDefaultVariantIsPrimary() {
        val defaultProps = FloatingActionButtonDefinition.definition.createDefaultProps()
        val variantVal = defaultProps[FloatingActionButtonDefinition.Variant.id] as? PropertyValue.EnumValue
        assertEquals(FabVariant.Primary.name, variantVal?.name)

        val freshNode = DesignNode(
            id = NodeId("fab-1"),
            kind = ComponentKind.FloatingActionButton,
            props = defaultProps
        )
        assertEquals(FabVariant.Primary, FloatingActionButtonDefinition.resolveVariant(freshNode))
    }

    @Test
    fun testLegacyNodeWithoutVariantResolvesToDefaults() {
        val legacyCard = DesignNode(
            id = NodeId("legacy-card"),
            kind = ComponentKind.Card,
            props = emptyMap()
        )
        assertEquals(CardVariant.Filled, CardDefinition.resolveVariant(legacyCard))

        val legacyFab = DesignNode(
            id = NodeId("legacy-fab"),
            kind = ComponentKind.FloatingActionButton,
            props = emptyMap()
        )
        assertEquals(FabVariant.Primary, FloatingActionButtonDefinition.resolveVariant(legacyFab))
    }

    @Test
    fun testUnknownVariantNameFallsBackSafelyToDefault() {
        val unknownCard = DesignNode(
            id = NodeId("card-unknown"),
            kind = ComponentKind.Card,
            props = mapOf(CardDefinition.Variant.id to PropertyValue.EnumValue("NonExistentVariant"))
        )
        assertEquals(CardVariant.Filled, CardDefinition.resolveVariant(unknownCard))

        val unknownFab = DesignNode(
            id = NodeId("fab-unknown"),
            kind = ComponentKind.FloatingActionButton,
            props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue("NonExistentVariant"))
        )
        assertEquals(FabVariant.Primary, FloatingActionButtonDefinition.resolveVariant(unknownFab))
    }

    @Test
    fun testCardPropVisibilityPredicatePerVariant() {
        val cardDef = CardDefinition.definition

        val outlinedNode = DesignNode(
            id = NodeId("card-outlined"),
            kind = ComponentKind.Card,
            props = mapOf(CardDefinition.Variant.id to PropertyValue.EnumValue(CardVariant.Outlined.name))
        )
        assertTrue(cardDef.isPropVisible(outlinedNode, CardDefinition.BorderColor.id))
        assertTrue(cardDef.isPropVisible(outlinedNode, CardDefinition.BorderWidth.id))
        assertTrue(cardDef.isPropVisible(outlinedNode, CardDefinition.Elevation.id))
        assertTrue(cardDef.isPropVisible(outlinedNode, CardDefinition.ContainerColor.id))
        assertTrue(cardDef.isPropVisible(outlinedNode, CardDefinition.Shape.id))

        val filledNode = DesignNode(
            id = NodeId("card-filled"),
            kind = ComponentKind.Card,
            props = mapOf(CardDefinition.Variant.id to PropertyValue.EnumValue(CardVariant.Filled.name))
        )
        assertFalse(cardDef.isPropVisible(filledNode, CardDefinition.BorderColor.id))
        assertFalse(cardDef.isPropVisible(filledNode, CardDefinition.BorderWidth.id))
        assertTrue(cardDef.isPropVisible(filledNode, CardDefinition.Elevation.id))
        assertTrue(cardDef.isPropVisible(filledNode, CardDefinition.ContainerColor.id))
        assertTrue(cardDef.isPropVisible(filledNode, CardDefinition.Shape.id))

        val elevatedNode = DesignNode(
            id = NodeId("card-elevated"),
            kind = ComponentKind.Card,
            props = mapOf(CardDefinition.Variant.id to PropertyValue.EnumValue(CardVariant.Elevated.name))
        )
        assertFalse(cardDef.isPropVisible(elevatedNode, CardDefinition.BorderColor.id))
        assertFalse(cardDef.isPropVisible(elevatedNode, CardDefinition.BorderWidth.id))
        assertTrue(cardDef.isPropVisible(elevatedNode, CardDefinition.Elevation.id))
    }

    @Test
    fun testFabPropVisibilityAcrossAllVariants() {
        val fabDef = FloatingActionButtonDefinition.definition
        for (variant in FabVariant.entries) {
            val fabNode = DesignNode(
                id = NodeId("fab-${variant.name}"),
                kind = ComponentKind.FloatingActionButton,
                props = mapOf(FloatingActionButtonDefinition.Variant.id to PropertyValue.EnumValue(variant.name))
            )
            assertTrue(fabDef.isPropVisible(fabNode, FloatingActionButtonDefinition.ContainerColor.id))
            assertTrue(fabDef.isPropVisible(fabNode, FloatingActionButtonDefinition.ContentColor.id))
        }
    }

    @Test
    fun testPropertyValuesRetainedWhenSwitchingVariants() {
        val initialProps = mutableMapOf<io.github.chandu4221.m3stage.property.PropertyId, PropertyValue>(
            CardDefinition.Variant.id to PropertyValue.EnumValue(CardVariant.Outlined.name),
            CardDefinition.BorderColor.id to PropertyValue.ColorValue.Token(M3ColorToken.Primary),
            CardDefinition.BorderWidth.id to PropertyValue.DpValue(3f)
        )
        val nodeOutlined = DesignNode(id = NodeId("c-1"), kind = ComponentKind.Card, props = initialProps)

        // Switch to Filled
        val nodeFilled = nodeOutlined.copy(
            props = nodeOutlined.props + (CardDefinition.Variant.id to PropertyValue.EnumValue(CardVariant.Filled.name))
        )
        // Values still exist in props
        assertEquals(PropertyValue.ColorValue.Token(M3ColorToken.Primary), nodeFilled.props[CardDefinition.BorderColor.id])
        assertEquals(PropertyValue.DpValue(3f), nodeFilled.props[CardDefinition.BorderWidth.id])

        // Switch back to Outlined
        val nodeRestored = nodeFilled.copy(
            props = nodeFilled.props + (CardDefinition.Variant.id to PropertyValue.EnumValue(CardVariant.Outlined.name))
        )
        assertEquals(PropertyValue.ColorValue.Token(M3ColorToken.Primary), nodeRestored.props[CardDefinition.BorderColor.id])
        assertEquals(PropertyValue.DpValue(3f), nodeRestored.props[CardDefinition.BorderWidth.id])
    }
}
