package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode


/**
 * Main entry point for rendering a design tree.
 * Manages the registry of component renderers and provides the recursive walk function.
 */
object DesignRenderer {

    private val registry: Map<String, NodeRenderer> = mapOf(
        "Text" to TextRenderer(),
        "Button" to ButtonRenderer(),
        "Column" to ColumnRenderer(),
        "Row" to RowRenderer(),
        "Box" to BoxRenderer(),
        "Card" to CardRenderer()
    )

    /**
     * Render a design node tree as a Composable.
     *
     * @param node The root node to render
     */
    @Composable
    fun Render(node: DesignNode) {
        // Recursive render function
        @Composable
        fun renderNode(node: DesignNode) {
            val renderer = registry[node.type.value]

            if (renderer != null) {
                renderer.Render(node) { child ->
                    renderNode(child)
                }
            } else {
                // Fallback for unknown component types
                Text(
                    text = "Unknown: ${node.type.value}"
                )
            }
        }

        renderNode(node)
    }
}