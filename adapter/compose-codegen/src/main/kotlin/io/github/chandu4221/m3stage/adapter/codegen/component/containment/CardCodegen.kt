package io.github.chandu4221.m3stage.adapter.codegen.component.containment

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.CardDefinition
import io.github.chandu4221.m3stage.model.CardVariant
import io.github.chandu4221.m3stage.model.DesignNode

class CardCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentKind.Card),
        "Card"
    )

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val variant = CardDefinition.resolveVariant(node)
        val elevation = node[CardDefinition.Elevation]?.value ?: 0f
        val shapeCode = ThemeCodeResolver.resolveShape(node[CardDefinition.Shape])
        val containerColorCode = ThemeCodeResolver.resolveColor(node[CardDefinition.ContainerColor])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val targetClassName = when (variant) {
            CardVariant.Elevated -> ClassName(PackageNameResolver.resolve(ComponentKind.Card), "ElevatedCard")
            CardVariant.Filled -> ClassName(PackageNameResolver.resolve(ComponentKind.Card), "Card")
            CardVariant.Outlined -> ClassName(PackageNameResolver.resolve(ComponentKind.Card), "OutlinedCard")
        }

        val builder = CodeBlock.builder()
            .add("%T(\n", targetClassName)
            .indent()

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (containerColorCode != null) {
            val colorsMethod = when (variant) {
                CardVariant.Elevated -> "elevatedCardColors"
                CardVariant.Filled -> "cardColors"
                CardVariant.Outlined -> "outlinedCardColors"
            }
            builder.add("colors = %T.%L(containerColor = %L),\n", ComposeSymbols.CardDefaults, colorsMethod, containerColorCode)
        }

        if (elevation > 0f) {
            val elevationMethod = when (variant) {
                CardVariant.Elevated -> "elevatedCardElevation"
                CardVariant.Filled -> "cardElevation"
                CardVariant.Outlined -> "outlinedCardElevation"
            }
            builder.add("elevation = %T.%L(defaultElevation = %L.dp),\n", ComposeSymbols.CardDefaults, elevationMethod, elevation)
        }

        if (shapeCode != null) {
            builder.add("shape = %L,\n", shapeCode)
        }

        if (variant == CardVariant.Outlined) {
            val borderColorCode = ThemeCodeResolver.resolveColor(node[CardDefinition.BorderColor])
            val borderWidth = node[CardDefinition.BorderWidth]?.value
            if (borderColorCode != null) {
                val widthVal = borderWidth ?: 1f
                builder.add(
                    "border = %T(%L.dp, %L),\n",
                    ClassName("androidx.compose.foundation", "BorderStroke"),
                    widthVal,
                    borderColorCode
                )
            }
        }

        builder.unindent()
        if (node.children.isEmpty() && node.slots.isEmpty()) {
            builder.add(")\n")
        } else {
            builder.add(") {\n")
            builder.indent()
            node.allDirectChildren.forEach { child ->
                builder.add(walk(child))
            }
            builder.unindent()
            builder.add("}\n")
        }

        return builder.build()
    }
}
