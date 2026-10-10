package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.adapter.codegen.ThemeCodeResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class IconCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(PackageNameResolver.resolve(ComponentKind.Icon), "Icon")

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val iconName = node[ComponentCatalog.IconProps.IconName]?.value ?: "Default"
        val tintCode = ThemeCodeResolver.resolveColor(node[ComponentCatalog.IconProps.Tint])
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)

        val iconsDefaultClass = ClassName("androidx.compose.material.icons.Icons", "Default")

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("imageVector = %T.%L,\n", iconsDefaultClass, iconName)
            .add("contentDescription = null,\n")

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        if (tintCode != null) {
            builder.add("tint = %L,\n", tintCode)
        }

        builder.unindent().add(")\n")
        return builder.build()
    }
}