package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
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
        val spacing = node[ComponentCatalog.RowProps.Spacing]?.value ?: 0f
        val hArrangementName = node[ComponentCatalog.RowProps.HorizontalArrangement]?.name
        val vAlignName = node[ComponentCatalog.RowProps.VerticalAlignment]?.name

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        // Horizontal arrangement
        val hArrangementCode = when (hArrangementName) {
            HorizontalArrangementOption.Center.name -> CodeBlock.of("%T.Center", ComposeSymbols.Arrangement)
            HorizontalArrangementOption.End.name -> CodeBlock.of("%T.End", ComposeSymbols.Arrangement)
            HorizontalArrangementOption.SpaceBetween.name -> CodeBlock.of("%T.SpaceBetween", ComposeSymbols.Arrangement)
            HorizontalArrangementOption.SpaceAround.name -> CodeBlock.of("%T.SpaceAround", ComposeSymbols.Arrangement)
            HorizontalArrangementOption.SpaceEvenly.name -> CodeBlock.of("%T.SpaceEvenly", ComposeSymbols.Arrangement)
            else -> if (spacing > 0f) {
                CodeBlock.of("%T.spacedBy(%L.%M)", ComposeSymbols.Arrangement, spacing, ComposeSymbols.Dp)
            } else null
        }
        if (hArrangementCode != null) {
            builder.add("horizontalArrangement = %L,\n", hArrangementCode)
        }

        // Vertical alignment
        val vAlignCode = when (vAlignName) {
            VerticalAlignmentOption.Top.name -> CodeBlock.of("%T.Top", ComposeSymbols.Alignment)
            VerticalAlignmentOption.Bottom.name -> CodeBlock.of("%T.Bottom", ComposeSymbols.Alignment)
            else -> CodeBlock.of("%T.CenterVertically", ComposeSymbols.Alignment)
        }
        builder.add("verticalAlignment = %L,\n", vAlignCode)

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