package io.github.chandu4221.m3stage.adapter.codegen.component.navigation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.NavigationBarItemDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class NavigationBarItemCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.NavigationBarItem),
        "NavigationBarItem"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val selected = node[NavigationBarItemDefinition.Selected]?.value ?: false
        val enabled = node[NavigationBarItemDefinition.Enabled]?.value ?: true
        val indicatorColorCode = ThemeCodeResolver.resolveColor(node[NavigationBarItemDefinition.IndicatorColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val iconNodes = node.slots[NavigationBarItemDefinition.IconSlot.id] ?: emptyList()
        val labelNodes = node.slots[NavigationBarItemDefinition.LabelSlot.id] ?: emptyList()

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("selected = %L,\n", selected)
            .add("onClick = { },\n")

        if (!enabled) {
            builder.add("enabled = %L,\n", enabled)
        }

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (indicatorColorCode != null) {
            builder.add(
                "colors = %T.colors(indicatorColor = %L),\n",
                ClassName("androidx.compose.material3", "NavigationBarItemDefaults"),
                indicatorColorCode
            )
        }

        builder.add("icon = {\n")
            .indent()
        if (iconNodes.isNotEmpty()) {
            iconNodes.forEach { builder.add(walk(it)) }
        } else {
            builder.add(
                "%T(imageVector = androidx.compose.material.icons.Icons.Default.Star, contentDescription = null)\n",
                ClassName("androidx.compose.material3", "Icon")
            )
        }
        builder.unindent()
            .add("},\n")

        if (labelNodes.isNotEmpty()) {
            builder.add("label = {\n")
                .indent()
            labelNodes.forEach { builder.add(walk(it)) }
            builder.unindent()
                .add("},\n")
        }

        builder.unindent()
            .add(")\n")

        return builder.build()
    }
}
