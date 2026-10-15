package io.github.chandu4221.m3stage.adapter.codegen.component.actions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.FloatingActionButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class FloatingActionButtonCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.FloatingActionButton),
        "FloatingActionButton"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val containerColorCode = ThemeCodeResolver.resolveColor(node[FloatingActionButtonDefinition.ContainerColor])
        val contentColorCode = ThemeCodeResolver.resolveColor(node[FloatingActionButtonDefinition.ContentColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("onClick = { /* Read-only Visual Model */ },\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (containerColorCode != null) {
            builder.add("containerColor = %L,\n", containerColorCode)
        }

        if (contentColorCode != null) {
            builder.add("contentColor = %L,\n", contentColorCode)
        }

        builder.unindent()
        builder.add(") {\n")
        builder.indent()

        val iconNode = node.slots[FloatingActionButtonDefinition.IconSlot.id]?.firstOrNull()
        if (iconNode != null) {
            builder.add(walk(iconNode))
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
