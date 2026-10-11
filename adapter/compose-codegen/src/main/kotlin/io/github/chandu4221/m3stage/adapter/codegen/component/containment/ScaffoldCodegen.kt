package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition
import io.github.chandu4221.m3stage.model.DesignNode

class ScaffoldCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Scaffold),
        "Scaffold"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val containerColorCode = ThemeCodeResolver.resolveColor(node[ScaffoldDefinition.ContainerColor])
        
        // Slot resolution with backward-compatible fallback to raw children
        val topBarNode = node.slots[ScaffoldDefinition.TopBarSlot.id]?.firstOrNull()
            ?: node.children.firstOrNull { it.kind == ComponentKind.TopAppBar }
        val bottomBarNode = node.slots[ScaffoldDefinition.BottomBarSlot.id]?.firstOrNull()
        val fabNode = node.slots[ScaffoldDefinition.FloatingActionButtonSlot.id]?.firstOrNull()
        val contentChildren = node.slots[ScaffoldDefinition.ContentSlot.id]
            ?: node.children.filter { it != topBarNode && it != bottomBarNode && it != fabNode }

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()

        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)
        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        } else {
            builder.add("modifier = %T.%M(),\n", ComposeSymbols.Modifier, ComposeSymbols.fillMaxSize)
        }

        if (containerColorCode != null) {
            builder.add("containerColor = %L,\n", containerColorCode)
        }

        if (topBarNode != null) {
            builder.add("topBar = {\n")
            builder.indent()
            builder.add(walk(topBarNode))
            builder.unindent()
            builder.add("},\n")
        }

        if (bottomBarNode != null) {
            builder.add("bottomBar = {\n")
            builder.indent()
            builder.add(walk(bottomBarNode))
            builder.unindent()
            builder.add("},\n")
        }

        if (fabNode != null) {
            builder.add("floatingActionButton = {\n")
            builder.indent()
            builder.add(walk(fabNode))
            builder.unindent()
            builder.add("},\n")
        }

        builder.unindent()
            .add(") { innerPadding ->\n")
            .indent()

        builder.add("%T(\n", ComposeSymbols.Box)
        builder.indent()
        builder.add(
            "modifier = %T.%M().%M(innerPadding),\n",
            ComposeSymbols.Modifier,
            ComposeSymbols.fillMaxSize,
            ComposeSymbols.padding
        )
        builder.unindent()
        builder.add(") {\n")
        builder.indent()

        contentChildren.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent()
        builder.add("}\n")

        builder.unindent()
        builder.add("}\n")

        return builder.build()
    }
}
