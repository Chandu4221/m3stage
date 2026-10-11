package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind

object SpacerDefinition {
    val definition = ComponentDefinition(
        kind = ComponentKind.Spacer,
        category = ComponentCategory.Layouts,
        isContainer = false,
        properties = emptyList()
    )
}