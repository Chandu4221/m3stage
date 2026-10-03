package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.ComponentTypes
import io.github.chandu4221.m3stage.model.DesignNode

class ColumnCodegen : ComponentCodegen {
    override val className = ClassName(
        PackageNameResolver.resolve(ComponentTypes.Column),
        "Column"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val builder = CodeBlock.builder()
        builder.beginControlFlow("%T", className)
        node.children.forEach { child ->
            builder.add(walk(child))
        }
        builder.endControlFlow()
        return builder.build()
    }
}