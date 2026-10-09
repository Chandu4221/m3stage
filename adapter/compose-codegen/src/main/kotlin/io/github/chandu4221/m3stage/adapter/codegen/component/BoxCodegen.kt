package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.ContentAlignmentOption

class BoxCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Box),
        "Box"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val alignmentName = node[ComponentCatalog.BoxProps.ContentAlignment]?.name

        val alignmentCode = when (alignmentName) {
            ContentAlignmentOption.TopStart.name -> CodeBlock.of("%T.TopStart", ComposeSymbols.Alignment)
            ContentAlignmentOption.TopCenter.name -> CodeBlock.of("%T.TopCenter", ComposeSymbols.Alignment)
            ContentAlignmentOption.TopEnd.name -> CodeBlock.of("%T.TopEnd", ComposeSymbols.Alignment)
            ContentAlignmentOption.CenterStart.name -> CodeBlock.of("%T.CenterStart", ComposeSymbols.Alignment)
            ContentAlignmentOption.Center.name -> CodeBlock.of("%T.Center", ComposeSymbols.Alignment)
            ContentAlignmentOption.CenterEnd.name -> CodeBlock.of("%T.CenterEnd", ComposeSymbols.Alignment)
            ContentAlignmentOption.BottomStart.name -> CodeBlock.of("%T.BottomStart", ComposeSymbols.Alignment)
            ContentAlignmentOption.BottomCenter.name -> CodeBlock.of("%T.BottomCenter", ComposeSymbols.Alignment)
            ContentAlignmentOption.BottomEnd.name -> CodeBlock.of("%T.BottomEnd", ComposeSymbols.Alignment)
            else -> null
        }

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (alignmentCode != null) {
            builder.add("contentAlignment = %L,\n", alignmentCode)
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