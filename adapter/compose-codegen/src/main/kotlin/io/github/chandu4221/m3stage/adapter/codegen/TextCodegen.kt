package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.PropVal

class TextCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentTypes.Text),
        "Text"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val textVal = node[ComponentCatalog.TextProps.TextContent]
        val textStr = (textVal as? PropVal.Str)?.value ?: "Sample Text"

        return CodeBlock.builder()
            .add("%T(text = %S)\n", className, textStr)
            .build()
    }
}