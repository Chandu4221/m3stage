package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class CardCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Card),
        "Card"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val elevation = node[CardDefinition.Elevation]?.value ?: 0f
        val shapeCode = ThemeCodeResolver.resolveShape(node[CardDefinition.Shape])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (elevation > 0f) {
            builder.add("elevation = %T.cardElevation(defaultElevation = %L.dp),\n", ComposeSymbols.CardDefaults, elevation)
        }

        if (shapeCode != null) {
            builder.add("shape = %L,\n", shapeCode)
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
