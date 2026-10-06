package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.PropVal

class ButtonCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentTypes.Button),
        "Button"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val textVal = node[ComponentCatalog.ButtonProps.TextContent]
        val textStr = (textVal as? PropVal.Str)?.value ?: "Button"

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("onClick = { /* TODO: Hook action */ }\n")
            .unindent()
            .add(") {\n")
            .indent()

        // Render children (usually a Text node)
        node.children.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent().add("}\n")
        return builder.build()
    }
}