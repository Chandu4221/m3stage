package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.ThemeCodeResolver
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
        val containerColor = ThemeCodeResolver.resolveColor(node[ComponentCatalog.ButtonProps.ContainerColor])
        val contentColor = ThemeCodeResolver.resolveColor(node[ComponentCatalog.ButtonProps.ContentColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("onClick = { /* TODO: Action */ },\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (!enabled) {
            builder.add("enabled = false,\n")
        }

        if (containerColor != null || contentColor != null) {
            builder.add("colors = %T.buttonColors(\n", ComposeSymbols.ButtonDefaults)
            builder.indent()
            if (containerColor != null) {
                builder.add("containerColor = %L,\n", containerColor)
            }
            if (contentColor != null) {
                builder.add("contentColor = %L,\n", contentColor)
            }
            builder.unindent()
            builder.add("),\n")
        }

        builder.unindent()
            .add(") {\n")
            .indent()

        node.children.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent().add("}\n")
        return builder.build()
    }
}