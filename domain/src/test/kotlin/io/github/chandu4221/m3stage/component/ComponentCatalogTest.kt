package io.github.chandu4221.m3stage.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ComponentCatalogTest {

    @Test
    fun testCatalogRegistersAllComponentKinds() {
        assertEquals(
            ComponentKind.entries.size,
            ComponentCatalog.all.size,
            "ComponentCatalog must contain definitions for all 11 ComponentKind values"
        )

        ComponentKind.entries.forEach { kind ->
            val def = ComponentCatalog[kind]
            assertEquals(kind, def.kind)
        }
    }

    @Test
    fun testAllDefinitionsCreateValidDefaultProps() {
        ComponentCatalog.all.forEach { def ->
            val defaultProps = def.createDefaultProps()
            def.descriptors.forEach { descriptor ->
                assertNotNull(
                    defaultProps[descriptor.key.id],
                    "Default props for ${def.kind} should contain key ${descriptor.key.id.value}"
                )
            }
        }
    }
}