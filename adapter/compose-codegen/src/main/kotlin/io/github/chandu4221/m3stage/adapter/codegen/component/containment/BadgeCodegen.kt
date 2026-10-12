package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.BadgeDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class BadgeCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Badge),
        "Badge"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val containerColorCode = ThemeCodeResolver.resolveColor(node[BadgeDefinition.ContainerColor])
        val contentColorCode = ThemeCodeResolver.resolveColor(node[BadgeDefinition.ContentColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

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
        val children = node.allDirectChildren
        if (children.isEmpty()) {
            builder.add(")\n")
        } else {
            builder.add(") {\n")
            builder.indent()
            children.forEach { child ->
                builder.add(walk(child))
            }
            builder.unindent()
            builder.add("}\n")
        }

        return builder.build()
    }
}
