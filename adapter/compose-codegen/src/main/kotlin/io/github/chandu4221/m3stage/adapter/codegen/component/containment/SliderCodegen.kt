package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.SliderDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class SliderCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Slider),
        "Slider"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val value = node[SliderDefinition.Value]?.value ?: 50f
        val enabled = node[SliderDefinition.Enabled]?.value ?: true
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("value = %Lf,\n", value)
            .add("onValueChange = { /* Read-only Visual Model */ },\n")
            .add("valueRange = 0f..100f,\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (!enabled) {
            builder.add("enabled = false,\n")
        }

        builder.unindent()
        builder.add(")\n")

        return builder.build()
    }
}
