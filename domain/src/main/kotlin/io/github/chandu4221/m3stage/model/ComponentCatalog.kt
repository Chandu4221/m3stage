package io.github.chandu4221.m3stage.model

object ComponentCatalog {

    private val definitions = listOf(
        // DISPLAY
        ComponentDefinition(
            type = ComponentTypes.Text,
            category = ComponentCategory.DISPLAY,
            displayName = "Text",
            defaultProps = mapOf("text" to "Sample Text"),
            isContainer = false
        ),
        // INPUTS
        ComponentDefinition(
            type = ComponentTypes.Button,
            category = ComponentCategory.INPUTS,
            displayName = "Button",
            defaultProps = mapOf("enabled" to "true"),
            defaultChildTypes = listOf(ComponentTypes.Text),
            isContainer = true
        ),
        // LAYOUTS
        ComponentDefinition(
            type = ComponentTypes.Column,
            category = ComponentCategory.LAYOUTS,
            displayName = "Column",
            defaultProps = emptyMap(),
            isContainer = true
        ),
        ComponentDefinition(
            type = ComponentTypes.Row,
            category = ComponentCategory.LAYOUTS,
            displayName = "Row",
            defaultProps = emptyMap(),
            isContainer = true
        ),
        ComponentDefinition(
            type = ComponentTypes.Box,
            category = ComponentCategory.LAYOUTS,
            displayName = "Box",
            defaultProps = emptyMap(),
            isContainer = true
        ),
        // SURFACES
        ComponentDefinition(
            type = ComponentTypes.Card,
            category = ComponentCategory.SURFACES,
            displayName = "Card",
            defaultProps = mapOf("elevation" to "1"),
            defaultChildTypes = listOf(ComponentTypes.Column),
            isContainer = true
        )
    )

    /**
     * Get all definitions grouped by category.
     * Perfect for rendering a grouped Palette UI.
     */
    val groupedByCategory: Map<ComponentCategory, List<ComponentDefinition>> =
        definitions.groupBy { it.category }

    /**
     * Look up a specific component by its type.
     */
    fun getByType(type: ComponentType): ComponentDefinition? {
        return definitions.find { it.type == type }
    }
}