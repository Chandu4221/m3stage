package io.github.chandu4221.m3stage.adapter.codegen.component.actions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class ButtonCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Button),
        "Button"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val isEnabled = node[ButtonDefinition.Enabled]?.value ?: true
        val containerColorCode = ThemeCodeResolver.resolveColor(node[ButtonDefinition.ContainerColor])
        val contentColorCode = ThemeCodeResolver.resolveColor(node[ButtonDefinition.ContentColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("onClick = { /* Read-only Visual Model */ },\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (!isEnabled) {
            builder.add("enabled = false,\n")
        }

        if (containerColorCode != null || contentColorCode != null) {
            builder.add("colors = %T.buttonColors(\n", ComposeSymbols.ButtonDefaults)
            builder.indent()
            if (containerColorCode != null) {
                builder.add("containerColor = %L,\n", containerColorCode)
            }
            if (contentColorCode != null) {
                builder.add("contentColor = %L,\n", contentColorCode)
            }
            builder.unindent()
            builder.add("),\n")
        }

        builder.unindent()
        builder.add(") {\n")
        builder.indent()

        // Slot-based rendering: leadingIcon, label, trailingIcon, with allDirectChildren fallback
        val leadingIcon = node.slots[ButtonDefinition.LeadingIconSlot.id]?.firstOrNull()
        val label = node.slots[ButtonDefinition.LabelSlot.id]?.firstOrNull()
        val trailingIcon = node.slots[ButtonDefinition.TrailingIconSlot.id]?.firstOrNull()

        if (leadingIcon != null || label != null || trailingIcon != null) {
            leadingIcon?.let { builder.add(walk(it)) }
            label?.let { builder.add(walk(it)) }
            trailingIcon?.let { builder.add(walk(it)) }
        } else {
            node.allDirectChildren.forEach { child ->
                builder.add(walk(child))
            }
        }

        builder.unindent()
        builder.add("}\n")

        return builder.build()
    }
}
