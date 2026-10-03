package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.ComponentTypes
import io.github.chandu4221.m3stage.model.DesignNode


class CardCodegen : ComponentCodegen {
    override val className = ClassName(
        PackageNameResolver.resolve(ComponentTypes.Card),
        "Card"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val builder = CodeBlock.builder()

        val elevation = node.props["elevation"] ?: "1"

        builder.beginControlFlow(
            "%T(elevation = %L.dp)",
            className,
            elevation
        )
        node.children.forEach { child ->
            builder.add(walk(child))
        }
        builder.endControlFlow()

        return builder.build()
    }
}