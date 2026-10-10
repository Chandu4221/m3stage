package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class ScaffoldCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Scaffold),
        "Scaffold"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val containerColorCode = ThemeCodeResolver.resolveColor(node[ComponentCatalog.ScaffoldProps.ContainerColor])
        val topBarNode = node.children.firstOrNull { it.kind == ComponentKind.TopAppBar }
        val contentChildren = node.children.filter { it != topBarNode }

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("modifier = %T.%M(),\n", ComposeSymbols.Modifier, ComposeSymbols.fillMaxSize)

        if (containerColorCode != null) {
            builder.add("containerColor = %L,\n", containerColorCode)
        }

        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)
        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        } else {
            builder.add("modifier = %T.%M(),\n", ComposeSymbols.Modifier, ComposeSymbols.fillMaxSize)
        }

        if (topBarNode != null) {
            builder.add("topBar = {\n")
            builder.indent()
            builder.add(walk(topBarNode))
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