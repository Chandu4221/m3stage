package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.PropVal

class CardCodegen : ComponentCodegen {

    // 1. Resolve the main component class dynamically
    override val className: ClassName = ClassName(
        PackageNameResolver.resolve(ComponentTypes.Card),
        "Card"
    )

    // 2. Define ClassName constants for all nested utilities and top-level functions
    private val material3Package = PackageNameResolver.resolve(ComponentTypes.Card)
    private val cardDefaultsClassName = ClassName(material3Package, "CardDefaults")

    private val modifierClassName = ClassName("androidx.compose.ui", "Modifier")
    private val fillMaxWidthClassName = ClassName("androidx.compose.foundation.layout", "fillMaxWidth")
    private val paddingClassName = ClassName("androidx.compose.foundation.layout", "padding")

    override fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock {
        val elevationVal = node[ComponentCatalog.CardProps.Elevation]
        val elevation = (elevationVal as? PropVal.DpVal)?.value ?: 1f

        val builder = CodeBlock.builder()
            .add("%T(\n", className)
            .indent()
            // Use %T for CardDefaults
            .add("elevation = %T.cardElevation(defaultElevation = %L.dp),\n", cardDefaultsClassName, elevation)

        if (node.children.isNotEmpty()) {
            // Use %T for Modifier and its extension functions
            builder.add("modifier = %T.%T().%T(16.dp)\n", modifierClassName, fillMaxWidthClassName, paddingClassName)
        }

        builder.unindent().add(") {\n").indent()

        node.children.forEach { child ->
            builder.add(walk(child))
        }

        builder.unindent().add("}\n")
        return builder.build()
    }
}