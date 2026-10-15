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

    private val railItemClassName = ClassName(
        "androidx.compose.material3",
        "NavigationRailItem"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val selected = node[NavigationBarItemDefinition.Selected]?.value ?: false
        val enabled = node[NavigationBarItemDefinition.Enabled]?.value ?: true
        val indicatorColorCode = ThemeCodeResolver.resolveColor(node[NavigationBarItemDefinition.IndicatorColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val iconNodes = node.slots[NavigationBarItemDefinition.IconSlot.id] ?: emptyList()
        val labelNodes = node.slots[NavigationBarItemDefinition.LabelSlot.id] ?: emptyList()

        val isRail = io.github.chandu4221.m3stage.adapter.codegen.resolver.AdaptiveCodegenContext.isRail
        val targetClassName = if (isRail) railItemClassName else className
        val defaultsClassName = if (isRail) {
            ClassName("androidx.compose.material3", "NavigationRailItemDefaults")
        } else {
            ClassName("androidx.compose.material3", "NavigationBarItemDefaults")
        }

        val builder = CodeBlock.builder()
            .add("%T(\n", targetClassName)
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
                defaultsClassName,
                indicatorColorCode
            )
        }

        builder.add("icon = {\n")
            .indent()
        if (iconNodes.isNotEmpty()) {
            iconNodes.forEach { builder.add(walk(it)) }
        } else {
            builder.add(
                "%T(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = %S)\n",
                ClassName("androidx.compose.material3", "Icon"),
                "Add"
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
