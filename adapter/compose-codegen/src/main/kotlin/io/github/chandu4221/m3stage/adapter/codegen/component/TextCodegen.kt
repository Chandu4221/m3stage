package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class TextCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Text),
        "Text"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val textStr = node[ComponentCatalog.TextProps.TextContent]?.value ?: "Sample Text"

        return CodeBlock.builder()
            .add("%T(text = %S)\n", className, textStr)
            .build()
    }
}