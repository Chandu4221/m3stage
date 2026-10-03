package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import io.github.chandu4221.m3stage.model.DesignNode


/**
 * Contract for component-specific code generators.
 *
 * Returns a CodeBlock instead of a String to allow the parent
 * to safely manage imports, indentation, and structural scoping.
 */
interface ComponentCodegen {
    /** The fully qualified Compose class this codegen generates. */
    val className: ClassName
    fun generate(node: DesignNode, walk: (DesignNode) -> CodeBlock): CodeBlock
}