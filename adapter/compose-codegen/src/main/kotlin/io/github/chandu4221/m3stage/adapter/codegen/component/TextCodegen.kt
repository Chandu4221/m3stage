package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.*
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.TextOverflowOption

class TextCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Text),
        "Text"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val textStr = node[ComponentCatalog.TextProps.TextContent]?.value ?: "Sample Text"
        val colorCode = ThemeCodeResolver.resolveColor(node[ComponentCatalog.TextProps.Color])
        val typoCode = ThemeCodeResolver.resolveTypography(node[ComponentCatalog.TextProps.Typography])
        val overflowName = node[ComponentCatalog.TextProps.Overflow]?.name
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("text = %S,\n", textStr)

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }
        if (colorCode != null) {
            builder.add("color = %L,\n", colorCode)
        }
        if (typoCode != null) {
            builder.add("style = %L,\n", typoCode)
        }
        if (overflowName != null && overflowName == TextOverflowOption.Ellipsis.name) {
            builder.add("overflow = %T.Ellipsis,\n", ComposeSymbols.TextOverflow)
        }

        builder.unindent().add(")\n")
        return builder.build()
    }
}