package io.github.chandu4221.m3stage.adapter.codegen.component.navigation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.TopAppBarDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.property.PropertyValue

class TopAppBarCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.TopAppBar),
        "TopAppBar"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val titleText = node[TopAppBarDefinition.Title]?.value ?: ""
        val containerColorCode = ThemeCodeResolver.resolveColor(node[TopAppBarDefinition.ContainerColor])
        val titleContentColorCode = ThemeCodeResolver.resolveColor(node[TopAppBarDefinition.TitleContentColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        // Slot resolution: title, navigationIcon, actions
        val titleNode = node.slots[TopAppBarDefinition.TitleSlot.id]?.firstOrNull()
        val navIconNode = node.slots[TopAppBarDefinition.NavigationIconSlot.id]?.firstOrNull()
        val actionNodes = node.slots[TopAppBarDefinition.ActionsSlot.id] ?: node.children

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        // 1. Title slot
        if (titleNode != null) {
            builder.add("title = {\n")
            builder.indent()
            builder.add(walk(titleNode))
            builder.unindent()
            builder.add("},\n")
        } else {
            builder.add("title = { %T(%S) },\n", ComposeSymbols.Text, titleText)
        }

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        // 2. Navigation Icon slot
        if (navIconNode != null) {
            builder.add("navigationIcon = {\n")
            builder.indent()
            builder.add(walk(navIconNode))
            builder.unindent()
            builder.add("},\n")
        }

        // 3. Actions slot
        if (actionNodes.isNotEmpty()) {
            builder.add("actions = {\n")
            builder.indent()
            actionNodes.forEach { actionChild ->
                builder.add(walk(actionChild))
            }
            builder.unindent()
            builder.add("},\n")
        }

        // 4. Colors
        if (containerColorCode != null || titleContentColorCode != null) {
            builder.add("colors = %T.topAppBarColors(\n", ComposeSymbols.TopAppBarDefaults)
            builder.indent()
            if (containerColorCode != null) {
                builder.add("containerColor = %L,\n", containerColorCode)
            }
            if (titleContentColorCode != null) {
                builder.add("titleContentColor = %L,\n", titleContentColorCode)
            }
            builder.unindent()
            builder.add("),\n")
        }

        builder.unindent()
        builder.add(")\n")

        return builder.build()
    }
}
