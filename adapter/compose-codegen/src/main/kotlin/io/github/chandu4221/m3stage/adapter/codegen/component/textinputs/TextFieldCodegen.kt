package io.github.chandu4221.m3stage.adapter.codegen.component.textinputs

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.TextFieldDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class TextFieldCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.TextField),
        "TextField"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val value = node[TextFieldDefinition.Value]?.value ?: ""
        val label = node[TextFieldDefinition.Label]?.value ?: ""
        val placeholder = node[TextFieldDefinition.Placeholder]?.value ?: ""
        val isSingleLine = node[TextFieldDefinition.SingleLine]?.value ?: true
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("value = %S,\n", value)
            .add("onValueChange = { /* Read-only Visual Model */ },\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (label.isNotBlank()) {
            builder.add("label = { %T(%S) },\n", ComposeSymbols.Text, label)
        }

        if (placeholder.isNotBlank()) {
            builder.add("placeholder = { %T(%S) },\n", ComposeSymbols.Text, placeholder)
        }

        if (isSingleLine) {
            builder.add("singleLine = true,\n")
        }

        builder.unindent()
            .add(")\n")

        return builder.build()
    }
}
