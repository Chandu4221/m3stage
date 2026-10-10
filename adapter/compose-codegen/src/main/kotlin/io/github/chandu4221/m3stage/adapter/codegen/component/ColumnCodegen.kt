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
import io.github.chandu4221.m3stage.property.HorizontalAlignmentOption
import io.github.chandu4221.m3stage.property.VerticalArrangementOption

class ColumnCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Column),
        "Column"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val spacing = node[ComponentCatalog.ColumnProps.Spacing]?.value ?: 0f
        val hAlignName = node[ComponentCatalog.ColumnProps.HorizontalAlignment]?.name
        val vArrangementName = node[ComponentCatalog.ColumnProps.VerticalArrangement]?.name
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)
        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        // Horizontal alignment
        val hAlignCode = when (hAlignName) {
            HorizontalAlignmentOption.CenterHorizontally.name -> CodeBlock.of(
                "%T.CenterHorizontally",
                ComposeSymbols.Alignment
            )

            HorizontalAlignmentOption.End.name -> CodeBlock.of("%T.End", ComposeSymbols.Alignment)
            else -> null
        }
        if (hAlignCode != null) {
            builder.add("horizontalAlignment = %L,\n", hAlignCode)
        }

        // Vertical arrangement
        val vArrangementCode = when (vArrangementName) {
            VerticalArrangementOption.Center.name -> CodeBlock.of("%T.Center", ComposeSymbols.Arrangement)
            VerticalArrangementOption.Bottom.name -> CodeBlock.of("%T.Bottom", ComposeSymbols.Arrangement)
            VerticalArrangementOption.SpaceBetween.name -> CodeBlock.of("%T.SpaceBetween", ComposeSymbols.Arrangement)
            VerticalArrangementOption.SpaceAround.name -> CodeBlock.of("%T.SpaceAround", ComposeSymbols.Arrangement)
            VerticalArrangementOption.SpaceEvenly.name -> CodeBlock.of("%T.SpaceEvenly", ComposeSymbols.Arrangement)
            else -> if (spacing > 0f) {
                CodeBlock.of("%T.spacedBy(%L.%M)", ComposeSymbols.Arrangement, spacing, ComposeSymbols.Dp)
            } else null
        }
        if (vArrangementCode != null) {
            builder.add("verticalArrangement = %L,\n", vArrangementCode)
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