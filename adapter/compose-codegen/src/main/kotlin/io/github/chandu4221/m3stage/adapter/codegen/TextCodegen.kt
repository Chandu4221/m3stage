package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.ComponentTypes
import io.github.chandu4221.m3stage.model.DesignNode

class TextCodegen : ComponentCodegen {
    override val className = ClassName(
        PackageNameResolver.resolve(ComponentTypes.Text),
        "Text"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val textValue = node.props["text"] ?: ""
        return CodeBlock.builder()
            .addStatement("%T(text = %S)", className, textValue)
            .build()
    }
}