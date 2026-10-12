package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.property.*
import io.github.chandu4221.m3stage.theme.M3ColorToken

object NavigationBarDefinition {
    val ContainerColor = PropertyKey.ColorKey(PropertyId("navigationBar.containerColor"))

    val definition = ComponentDefinition(
        kind = ComponentKind.NavigationBar,
        category = ComponentCategory.Navigation,
        isContainer = true,
        properties = listOf(
            PropertySpec.ColorPicker(
                key = ContainerColor,
                displayName = "Container Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.SurfaceContainer)
            )
        ),
        allowedChildren = setOf(ComponentKind.IconButton, ComponentKind.Icon, ComponentKind.Text),
        slotOnly = true
    )
}