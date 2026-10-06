package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class ButtonCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Button),
        "Button"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val enabled = node[ComponentCatalog.ButtonProps.Enabled]?.value ?: true

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("onClick = { /* TODO: Hook action */ },\n")

        if (!enabled) {
            builder.add("enabled = false,\n")
        }

        builder.unindent()
            .add(") {\n")
            .indent()

        // Render children (Text and Icon atoms live as children)
        node.children.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent().add("}\n")
        return builder.build()
    }
}