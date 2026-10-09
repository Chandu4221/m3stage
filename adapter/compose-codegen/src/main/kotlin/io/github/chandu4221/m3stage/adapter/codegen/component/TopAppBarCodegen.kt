package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class TopAppBarCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.TopAppBar),
        "TopAppBar"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val title = node[ComponentCatalog.TopAppBarProps.Title]?.value ?: "Title"
        val containerColorCode = ThemeCodeResolver.resolveColor(node[ComponentCatalog.TopAppBarProps.ContainerColor])
        val titleContentColorCode = ThemeCodeResolver.resolveColor(node[ComponentCatalog.TopAppBarProps.TitleContentColor])
        val actions = node.children.filter { it.kind == ComponentKind.Icon || it.kind == ComponentKind.Button }

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("title = { %T(%S) },\n", ComposeSymbols.Text, title)

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

        if (actions.isNotEmpty()) {
            builder.add("actions = {\n")
            builder.indent()
            actions.forEach { child ->
                builder.add(walk(child))
            }
            builder.unindent()
            builder.add("},\n")
        }

        builder.unindent()
            .add(")\n")

        return builder.build()
    }
}