package io.github.chandu4221.m3stage.adapter.codegen.component.layouts

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class SpacerCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Spacer),
        "Spacer"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        } else {
            builder.add("modifier = androidx.compose.ui.Modifier,\n")
        }

        builder.unindent()
        builder.add(")\n")

        return builder.build()
    }
}
