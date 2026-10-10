package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.*
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class CardCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Card),
        "Card"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val elevation = node[ComponentCatalog.CardProps.Elevation]?.value ?: 1f
        val containerColor = ThemeCodeResolver.resolveColor(node[ComponentCatalog.CardProps.ContainerColor])
        val shape = ThemeCodeResolver.resolveShape(node[ComponentCatalog.CardProps.Shape])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }
        // ... (rest unchanged)

        if (containerColor != null) {
            builder.add(
                "colors = %T.cardColors(containerColor = %L),\n",
                ComposeSymbols.CardDefaults,
                containerColor
            )
        }

        if (shape != null) {
            builder.add("shape = %L,\n", shape)
        }

        builder.unindent()
            .add(") {\n")
            .indent()

        node.children.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent().add("}\n")
        return builder.build()
    }
}