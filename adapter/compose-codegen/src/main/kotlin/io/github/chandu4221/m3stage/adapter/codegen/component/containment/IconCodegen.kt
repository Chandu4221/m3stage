package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.IconDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class IconCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Icon),
        "Icon"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val glyph = IconDefinition.resolveGlyph(node)
        val tintCode = ThemeCodeResolver.resolveColor(node[IconDefinition.Tint])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("imageVector = androidx.compose.material.icons.Icons.Default.%L,\n", glyph.name)
            .add("contentDescription = %S,\n", glyph.name)

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (tintCode != null) {
            builder.add("tint = %L,\n", tintCode)
        }

        builder.unindent()
            .add(")\n")

        return builder.build()
    }
}
