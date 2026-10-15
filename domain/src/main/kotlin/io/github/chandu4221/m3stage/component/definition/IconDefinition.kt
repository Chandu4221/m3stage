package io.github.chandu4221.m3stage.component.definition

import io.github.chandu4221.m3stage.component.ComponentCategory
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.IconGlyph
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyKey
import io.github.chandu4221.m3stage.property.PropertySpec
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.theme.M3ColorToken

object IconDefinition {
    val Glyph = PropertyKey.EnumKey(PropertyId("icon.glyph"))
    val IconName = PropertyKey.StringKey(PropertyId("icon.name"))
    val Tint = PropertyKey.ColorKey(PropertyId("icon.tint"))

    fun resolveGlyph(node: DesignNode): IconGlyph {
        val enumVal = node[Glyph]?.name
        if (enumVal != null) {
            return IconGlyph.entries.find { it.name.equals(enumVal, ignoreCase = true) } ?: IconGlyph.Add
        }
        val nameVal = node[IconName]?.value
        if (nameVal != null) {
            return IconGlyph.entries.find { it.name.equals(nameVal, ignoreCase = true) } ?: IconGlyph.Add
        }
        return IconGlyph.Add
    }

    val definition = ComponentDefinition(
        kind = ComponentKind.Icon,
        category = ComponentCategory.Communication,
        properties = listOf(
            PropertySpec.EnumDropdown(
                key = Glyph,
                displayName = "Glyph",
                options = IconGlyph.entries.map { it.name },
                defaultValue = PropertyValue.EnumValue(IconGlyph.Add.name)
            ),
            PropertySpec.Text(
                key = IconName,
                displayName = "Icon Name",
                defaultValue = PropertyValue.StringValue(IconGlyph.Add.name)
            ),
            PropertySpec.ColorPicker(
                key = Tint,
                displayName = "Tint Color",
                defaultValue = PropertyValue.ColorValue.Token(M3ColorToken.Inherit)
            )
        ),
        defaultSubtreeFactory = { self, idGen ->
            DesignNode(
                id = idGen.nextNodeId(),
                kind = ComponentKind.Icon,
                props = self.createDefaultProps().toMutableMap().apply {
                    put(Glyph.id, PropertyValue.EnumValue(IconGlyph.Add.name))
                    put(IconName.id, PropertyValue.StringValue(IconGlyph.Add.name))
                }
            )
        }
    )
}