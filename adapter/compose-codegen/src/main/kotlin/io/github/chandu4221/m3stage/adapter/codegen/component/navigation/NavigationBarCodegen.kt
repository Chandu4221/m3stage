package io.github.chandu4221.m3stage.adapter.codegen.component.navigation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.AdaptiveCodegenContext
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

    private val railClassName = ClassName(
        "androidx.compose.material3",
        "NavigationRail"
    )

    private val windowSizeClassType = ClassName(
        "io.github.chandu4221.m3stage.model",
        "WindowSizeClass"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val containerColorCode = ThemeCodeResolver.resolveColor(node[NavigationBarDefinition.ContainerColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val items = node.slots[NavigationBarDefinition.ItemsSlot.id]
            ?.ifEmpty { null }
            ?: node.allDirectChildren.filter { it.kind == ComponentKind.NavigationBarItem }

        fun generateBar(isRail: Boolean): CodeBlock {
            val builder = CodeBlock.builder()
            val targetClassName = if (isRail) railClassName else className

            builder.add("%T(\n", targetClassName)
            builder.indent()

            if (modifierCode != null) {
                builder.add("modifier = %L,\n", modifierCode)
            }

            if (containerColorCode != null) {
                builder.add("containerColor = %L,\n", containerColorCode)
            }

            builder.unindent()
            if (items.isEmpty()) {
                builder.add(")\n")
            } else {
                builder.add(") {\n")
                builder.indent()
                items.forEach { child ->
                    builder.add(AdaptiveCodegenContext.withRailMode(isRail) { walk(child) })
                }
                builder.unindent()
                builder.add("}\n")
            }
            return builder.build()
        }

        if (AdaptiveCodegenContext.isAdaptiveScreen) {
            val builder = CodeBlock.builder()
            builder.beginControlFlow("when (windowSizeClass)")
            builder.beginControlFlow("%T.Expanded ->", windowSizeClassType)
            builder.add(generateBar(isRail = true))
            builder.endControlFlow()
            builder.beginControlFlow("else ->")
            builder.add(generateBar(isRail = false))
            builder.endControlFlow()
            builder.endControlFlow()
            return builder.build()
        } else {
            return generateBar(isRail = false)
        }
    }
}

