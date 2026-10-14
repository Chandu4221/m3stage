package io.github.chandu4221.m3stage.adapter.codegen.component.navigation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.NavigationBarDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class NavigationBarCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.NavigationBar),
        "NavigationBar"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val containerColorCode = ThemeCodeResolver.resolveColor(node[NavigationBarDefinition.ContainerColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (containerColorCode != null) {
            builder.add("containerColor = %L,\n", containerColorCode)
        }

        builder.unindent()
        val items = node.slots[NavigationBarDefinition.ItemsSlot.id]
            ?.ifEmpty { null }
            ?: node.allDirectChildren.filter { it.kind == ComponentKind.NavigationBarItem }

        if (items.isEmpty()) {
            builder.add(")\n")
        } else {
            builder.add(") {\n")
            builder.indent()
            items.forEach { child ->
                builder.add(walk(child))
            }
            builder.unindent()
            builder.add("}\n")
        }

        return builder.build()
    }
}
