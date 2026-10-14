package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.modifier.ModifierSpec

object SpacerDefinition {
    val definition = ComponentDefinition(
        kind = ComponentKind.Spacer,
        category = ComponentCategory.Layouts,
        isContainer = false,
        properties = emptyList(),
        defaultSubtreeFactory = { _, idGen ->
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Spacer,
                modifiers = listOf(ModifierSpec.Size(16f, 16f))
            )
        }
    )
}