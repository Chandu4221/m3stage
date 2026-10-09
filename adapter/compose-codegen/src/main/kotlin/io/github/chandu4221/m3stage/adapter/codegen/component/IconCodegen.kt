package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog.IconProps
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class IconCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(PackageNameResolver.resolve(ComponentKind.Icon), "Icon")

    override fun generate(
        node: DesignNode,
        walk: (DesignNode) -> CodeBlock
    ): CodeBlock {
        val iconName = (node.props[IconProps.IconName.id] as? PropertyValue.StringValue)?.value ?: "Star"
        val iconsDefault = ClassName("androidx.compose.material.icons", "Icons")

        val builder = CodeBlock.builder()
        builder.add("%T(\n", className)
        builder.indent()
        builder.add("imageVector = %T.Default.%L,\n", iconsDefault, iconName)
        builder.add("contentDescription = %S\n", iconName)
        builder.unindent()
        builder.add(")\n")
        return builder.build()
    }
}