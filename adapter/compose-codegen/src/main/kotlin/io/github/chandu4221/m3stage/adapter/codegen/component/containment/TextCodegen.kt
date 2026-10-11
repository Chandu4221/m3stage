package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.TextDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class TextCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Text),
        "Text"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val text = node[TextDefinition.TextContent]?.value ?: ""
        val colorCode = ThemeCodeResolver.resolveColor(node[TextDefinition.Color])
        val typographyCode = ThemeCodeResolver.resolveTypography(node[TextDefinition.Typography])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("text = %S,\n", text)

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (colorCode != null) {
            builder.add("color = %L,\n", colorCode)
        }

        if (typographyCode != null) {
            builder.add("style = %L,\n", typographyCode)
        }

        builder.unindent()
            .add(")\n")

        return builder.build()
    }
}
