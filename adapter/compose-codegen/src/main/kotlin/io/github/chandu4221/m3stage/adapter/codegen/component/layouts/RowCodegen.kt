package io.github.chandu4221.m3stage.adapter.codegen.component.layouts

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.definition.RowDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.HorizontalArrangementOption
import io.github.chandu4221.m3stage.property.VerticalAlignmentOption

class RowCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Row),
        "Row"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val spacing = node[RowDefinition.Spacing]?.value ?: 0f
        val horizontalArrangementOption = node[RowDefinition.HorizontalArrangement]?.name
        val verticalAlignmentOption = node[RowDefinition.VerticalAlignment]?.name
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (spacing > 0f) {
            builder.add("horizontalArrangement = %T.spacedBy(%L.dp),\n", ComposeSymbols.Arrangement, spacing)
        } else if (horizontalArrangementOption != null && horizontalArrangementOption != HorizontalArrangementOption.Start.name) {
            builder.add("horizontalArrangement = %T.%L,\n", ComposeSymbols.Arrangement, horizontalArrangementOption)
        }

        if (verticalAlignmentOption != null && verticalAlignmentOption != VerticalAlignmentOption.Top.name) {
            builder.add("verticalAlignment = %T.%L,\n", ComposeSymbols.Alignment, verticalAlignmentOption)
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
