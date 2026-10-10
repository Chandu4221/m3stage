package io.github.chandu4221.m3stage.adapter.codegen.component

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.PackageNameResolver
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode

class ImageCodegen : ComponentCodegen {
    override val className: ClassName = ClassName(PackageNameResolver.resolve(ComponentKind.Image), "Image")

    override fun generate(
        node: DesignNode,
        walk: (DesignNode) -> CodeBlock
    ): CodeBlock {
        val contentDesc = node[ComponentCatalog.ImageProps.ContentDescription]?.value
        val modifierCode = ModifierCodeResolver.generateModifierChain(node.modifiers)
        val iconsFilledClass = ClassName("androidx.compose.material.icons.Icons", "Filled")

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            .add("imageVector = %T.Star,\n", iconsFilledClass)
            .add("contentDescription = %S,\n", contentDesc)

        if (modifierCode != null) {
            builder.add("modifier = %L,\n", modifierCode)
        }

        builder.unindent().add(")\n")
        return builder.build()
    }
}