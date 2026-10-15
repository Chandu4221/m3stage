package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogColorInvariantTest {

    @Test
    fun testEveryColorPickerPropertyDefaultsToInheritToken() {
        val inheritDefault = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)
        var totalColorProps = 0

        for (def in ComponentCatalog.all) {
            val colorSpecs = def.properties.filterIsInstance<PropertySpec.ColorPicker>()
            for (spec in colorSpecs) {
                totalColorProps++
                assertEquals(
                    inheritDefault,
                    spec.defaultValue,
                    "Component '${def.kind.name}' property '${spec.displayName}' (${spec.key.id.value}) must default to M3ColorToken.Inherit"
                )
            }
        }

        assertTrue(totalColorProps > 0, "Expected at least one ColorPicker property across catalog")
    }

    @Test
    fun testDefaultPropsForEveryDefinitionProduceInheritForColorProps() {
        val inheritDefault = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)

        for (def in ComponentCatalog.all) {
            val defaultProps = def.createDefaultProps()
            val colorSpecs = def.properties.filterIsInstance<PropertySpec.ColorPicker>()

            for (spec in colorSpecs) {
                val propValue = defaultProps[spec.key.id]
                assertEquals(
                    inheritDefault,
                    propValue,
                    "Component '${def.kind.name}' createDefaultProps() for property '${spec.displayName}' must be Token(M3ColorToken.Inherit)"
                )
            }
        }
    }

    @Test
    fun testNoDefinitionContainsHardcodedColorDefaults() {
        for (def in ComponentCatalog.all) {
            for (spec in def.properties) {
                if (spec is PropertySpec.ColorPicker) {
                    val defaultVal = spec.defaultValue
                    assertTrue(
                        defaultVal is PropertyValue.ColorValue.Token && defaultVal.token == M3ColorToken.Inherit,
                        "Component '${def.kind.name}' property '${spec.key.id.value}' must not have hardcoded color: $defaultVal"
                    )
                }
            }
        }
    }
}
