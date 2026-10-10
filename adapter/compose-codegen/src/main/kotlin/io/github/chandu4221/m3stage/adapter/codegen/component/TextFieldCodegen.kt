package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class TextFieldCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(PackageNameResolver.resolve(ComponentKind.TextField), "TextField")

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val text = node[ComponentCatalog.TextFieldProps.Value]?.value ?: ""
        val label = node[ComponentCatalog.TextFieldProps.Label]?.value ?: "Label"
        val placeholder = node[ComponentCatalog.TextFieldProps.Placeholder]?.value
        val isSingleLine = node[ComponentCatalog.TextFieldProps.SingleLine]?.value ?: true
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("value = %S,\n", text)
            .add("onValueChange = { /* TODO */ },\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }
        // ... (rest unchanged)
        if (label.isNotEmpty()) {
            builder.add("label = { %T(%S) },\n", ComposeSymbols.Text, label)
        }
        if (placeholder?.isNotEmpty() == true) {
            builder.add("placeholder = { %T(%S) },\n", ComposeSymbols.Text, placeholder)
        }
        if (!isSingleLine) {
            builder.add("singleLine = false,\n")
        }

        builder.unindent()
            .add(")\n")
        return builder.build()
    }
}