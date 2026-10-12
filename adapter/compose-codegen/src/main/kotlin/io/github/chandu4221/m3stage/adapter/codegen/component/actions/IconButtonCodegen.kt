package io.github.chandu4221.m3stage.adapter.codegen.component.actions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.IconButtonDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class IconButtonCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.IconButton),
        "IconButton"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val enabled = node[IconButtonDefinition.Enabled]?.value ?: true
        val contentColorCode = ThemeCodeResolver.resolveColor(node[IconButtonDefinition.ContentColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("onClick = { /* Read-only Visual Model */ },\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (!enabled) {
            builder.add("enabled = false,\n")
        }

        if (contentColorCode != null) {
            builder.add("colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(contentColor = %L),\n", contentColorCode)
        }

        builder.unindent()
        builder.add(") {\n")
        builder.indent()

        val iconNode = node.slots[IconButtonDefinition.IconSlot.id]?.firstOrNull()
        if (iconNode != null) {
            builder.add(walk(iconNode))
        } else {
            node.allDirectChildren.forEach { child ->
                builder.add(walk(child))
            }
        }

        builder.unindent()
        builder.add("}\n")

        return builder.build()
    }
}
