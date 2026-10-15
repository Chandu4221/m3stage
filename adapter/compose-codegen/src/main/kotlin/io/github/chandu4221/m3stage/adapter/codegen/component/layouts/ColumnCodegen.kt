package io.github.chandu4221.m3stage.adapter.codegen.component.layouts

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.definition.ColumnDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.HorizontalAlignmentOption
import io.github.chandu4221.m3stage.property.VerticalArrangementOption

class ColumnCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Column),
        "Column"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val spacing = node[ColumnDefinition.Spacing]?.value ?: 0f
        val horizontalAlignmentOption = node[ColumnDefinition.HorizontalAlignment]?.name
        val verticalArrangementOption = node[ColumnDefinition.VerticalArrangement]?.name
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (spacing > 0f) {
            builder.add("verticalArrangement = %T.spacedBy(%L.dp),\n", ComposeSymbols.Arrangement, spacing)
        } else if (verticalArrangementOption != null && verticalArrangementOption != VerticalArrangementOption.Top.name) {
            builder.add("verticalArrangement = %T.%L,\n", ComposeSymbols.Arrangement, verticalArrangementOption)
        }

        if (horizontalAlignmentOption != null && horizontalAlignmentOption != HorizontalAlignmentOption.Start.name) {
            builder.add("horizontalAlignment = %T.%L,\n", ComposeSymbols.Alignment, horizontalAlignmentOption)
        }

        builder.unindent()
        if (node.children.isEmpty() && node.slots.isEmpty()) {
            builder.add(")\n")
        } else {
            builder.add(") {\n")
            builder.indent()
            node.allDirectChildren.forEach { child ->
                builder.add(walk(child))
            }
            builder.unindent()
            builder.add("}\n")
        }

        return builder.build()
    }
}
