package io.github.chandu4221.m3stage.adapter.codegen.component.layouts

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class BoxCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Box),
        "Box"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)
        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        builder.unindent()
        if (node.children.isEmpty() && node.slots.isEmpty()) {
            builder.add(")\n")
        } else {
            builder.add(") {\n")
            builder.indent()
            node.allDirectChildren.forEach { child ->
                builder.add(walk(child))
            }
            builder.unindent()
            builder.add("}\n")
        }

        return builder.build()
    }
}
