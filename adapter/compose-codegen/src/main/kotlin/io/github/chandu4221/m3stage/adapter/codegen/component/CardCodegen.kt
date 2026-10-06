package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
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

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            // %M is KotlinPoet for MemberName (extension properties and functions)
            .add(
                "elevation = %T.cardElevation(defaultElevation = %L.%M),\n",
                ComposeSymbols.CardDefaults,
                elevation,
                ComposeSymbols.Dp
            )
            .unindent()
            .add(") {\n")
            .indent()

        node.children.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent().add("}\n")
        return builder.build()
    }
}