package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.ThemeCodeResolver
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
        val containerColorCode = ThemeCodeResolver.resolveColor(node[ComponentCatalog.CardProps.ContainerColor])
        val shapeCode = ThemeCodeResolver.resolveShape(node[ComponentCatalog.CardProps.Shape])

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add(
                "elevation = %T.cardElevation(defaultElevation = %L.%M),\n",
                ComposeSymbols.CardDefaults,
                elevation,
                ComposeSymbols.Dp
            )

        if (containerColorCode != null) {
            builder.add(
                "colors = %T.cardColors(containerColor = %L),\n",
                ComposeSymbols.CardDefaults,
                containerColorCode
            )
        }

        if (shapeCode != null) {
            builder.add("shape = %L,\n", shapeCode)
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