package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.adapter.codegen.registry.ComponentCodegenRegistry
import io.github.chandu4221.m3stage.component.ComponentKind
import kotlin.test.Test
import kotlin.test.assertEquals

class RegistryCoverageTest {
    @Test
    fun testAllComponentKindsRegisteredInCodegen() {
        assertEquals(
            ComponentKind.entries.toSet(),
            ComponentCodegenRegistry.registeredKinds,
            "All ComponentKind entries must be registered in ComponentCodegenRegistry"
        )
    }
}
