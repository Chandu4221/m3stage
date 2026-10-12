package io.github.chandu4221.m3stage.adapter.codegen.component.layouts

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.HorizontalDividerDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class HorizontalDividerCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.HorizontalDivider),
        "HorizontalDivider"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val thickness = node[HorizontalDividerDefinition.Thickness]?.value ?: 1f
        val colorCode = ThemeCodeResolver.resolveColor(node[HorizontalDividerDefinition.Color])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (thickness != 1f) {
            builder.add("thickness = %L.dp,\n", thickness)
        }

        if (colorCode != null) {
            builder.add("color = %L,\n", colorCode)
        }

        builder.unindent()
        builder.add(")\n")

        return builder.build()
    }
}
