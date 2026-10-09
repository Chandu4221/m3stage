package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog.TextFieldProps
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class TextFieldCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(PackageNameResolver.resolve(ComponentKind.TextField), "TextField")

    override fun generate(
        node: DesignNode,
        walk: (DesignNode) -> CodeBlock
    ): CodeBlock {
        val textValue = (node.props[TextFieldProps.Value.id] as? PropertyValue.StringValue)?.value ?: ""
        val label = (node.props[TextFieldProps.Label.id] as? PropertyValue.StringValue)?.value ?: ""
        val singleLine = (node.props[TextFieldProps.SingleLine.id] as? PropertyValue.BooleanValue)?.value ?: true

        val textClass = ClassName("androidx.compose.material3", "Text")
        val builder = CodeBlock.builder()
        builder.add("%T(\n", className)
        builder.indent()
        builder.add("value = %S,\n", textValue)
        builder.add("onValueChange = {},\n")
        if (label.isNotEmpty()) {
            builder.add("label = { %T(%S) },\n", textClass, label)
        }
        builder.add("singleLine = %L\n", singleLine)
        builder.unindent()
        builder.add(")\n")
        return builder.build()
    }
}