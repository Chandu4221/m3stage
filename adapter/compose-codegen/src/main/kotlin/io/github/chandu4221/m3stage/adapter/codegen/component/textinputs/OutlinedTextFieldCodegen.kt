package io.github.chandu4221.m3stage.adapter.codegen.component.textinputs

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.OutlinedTextFieldDefinition
import io.github.chandu4221.m3stage.component.definition.TextFieldDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class OutlinedTextFieldCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.OutlinedTextField),
        "OutlinedTextField"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val value = node[OutlinedTextFieldDefinition.Value]?.value ?: ""
        val label = node[OutlinedTextFieldDefinition.Label]?.value ?: ""
        val placeholder = node[OutlinedTextFieldDefinition.Placeholder]?.value ?: ""
        val isSingleLine = node[OutlinedTextFieldDefinition.SingleLine]?.value ?: true
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val leadingIcon = node.slots[TextFieldDefinition.LeadingIconSlot.id]?.firstOrNull()
        val trailingIcon = node.slots[TextFieldDefinition.TrailingIconSlot.id]?.firstOrNull()
        val supportingText = node.slots[TextFieldDefinition.SupportingTextSlot.id]?.firstOrNull()

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

        if (leadingIcon != null) {
            builder.add("leadingIcon = {\n")
            builder.indent()
            builder.add(walk(leadingIcon))
            builder.unindent()
            builder.add("},\n")
        }

        if (trailingIcon != null) {
            builder.add("trailingIcon = {\n")
            builder.indent()
            builder.add(walk(trailingIcon))
            builder.unindent()
            builder.add("},\n")
        }

        if (supportingText != null) {
            builder.add("supportingText = {\n")
            builder.indent()
            builder.add(walk(supportingText))
            builder.unindent()
            builder.add("},\n")
        }

        if (isSingleLine) {
            builder.add("singleLine = true,\n")
        }

        builder.unindent()
        builder.add(")\n")

        return builder.build()
    }
}
