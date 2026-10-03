package io.github.chandu4221.m3stage.adapter.renderer

import androidx.compose.runtime.Composable
import io.github.chandu4221.m3stage.model.DesignNode


/**
 * Interface for rendering a DesignNode as a Composable.
 * Each Material 3 component implements this to provide its visual representation.
 */
interface NodeRenderer {
    /**
     * Render a design node as a Composable.
     *
     * @param node The design node to render
     * @param renderChild A function to recursively render child nodes
     */
    @Composable
    fun Render(
        node: DesignNode,
        renderChild: @Composable (DesignNode) -> Unit
    )
}