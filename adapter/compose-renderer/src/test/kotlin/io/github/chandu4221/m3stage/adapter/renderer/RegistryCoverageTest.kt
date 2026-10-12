package io.github.chandu4221.m3stage.adapter.renderer

import io.github.chandu4221.m3stage.adapter.renderer.registry.ComponentRendererRegistry
import io.github.chandu4221.m3stage.component.ComponentKind
import kotlin.test.Test
import kotlin.test.assertEquals

class RegistryCoverageTest {
    @Test
    fun testAllComponentKindsRegisteredInRenderer() {
        assertEquals(
            ComponentKind.entries.toSet(),
            ComponentRendererRegistry.registeredKinds,
            "All ComponentKind entries must be registered in ComponentRendererRegistry"
        )
    }
}
