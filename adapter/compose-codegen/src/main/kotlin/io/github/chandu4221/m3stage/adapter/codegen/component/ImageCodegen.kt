package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog.ImageProps
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class ImageCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(PackageNameResolver.resolve(ComponentKind.Image), "Image")

    override fun generate(
        node: DesignNode,
        walk: (DesignNode) -> CodeBlock
    ): CodeBlock {
        val desc = (node.props[ImageProps.ContentDescription.id] as? PropertyValue.StringValue)?.value ?: "Image"
        val iconsDefault = ClassName("androidx.compose.material.icons", "Icons")

        val builder = CodeBlock.builder()
        builder.add("%T(\n", className)
        builder.indent()
        builder.add("imageVector = %T.Default.Image,\n", iconsDefault)
        builder.add("contentDescription = %S\n", desc)
        builder.unindent()
        builder.add(")\n")
        return builder.build()
    }
}