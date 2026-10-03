package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.model.ComponentCatalog
import io.github.chandu4221.m3stage.model.ComponentCategory
import io.github.chandu4221.m3stage.model.ComponentType


/**
 * Single source of truth for Compose package names.
 * Maps a ComponentType → its fully qualified Compose package.
 *
 * If a component needs a custom package (not matching its category),
 * add a special case in the `customPackages` map.
 */
object PackageNameResolver {

    // Special cases where a component's package differs from its category default
    private val customPackages: Map<ComponentType, String> = emptyMap()

    fun resolve(type: ComponentType): String {
        // 1. Check for custom override first
        customPackages[type]?.let { return it }

        // 2. Fall back to category-based default
        val definition = ComponentCatalog.getByType(type)
            ?: error("Unknown component type: ${type.value}")

        return when (definition.category) {
            ComponentCategory.LAYOUTS -> "androidx.compose.foundation.layout"
            ComponentCategory.SURFACES,
            ComponentCategory.INPUTS,
            ComponentCategory.DISPLAY,
            ComponentCategory.NAVIGATION -> "androidx.compose.material3"
        }
    }
}