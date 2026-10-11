package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.component.definition.*

/**
 * Authoritative registry and catalog of all supported [ComponentDefinition]s in M3Stage.
 * Aggregates individual definitions and provides O(1) non-nullable lookups by [ComponentKind].
 */
object ComponentCatalog {

    val TextProps = TextDefinition
    val ButtonProps = ButtonDefinition

    val IconButtonProps = IconButtonDefinition

    val FloatingActionButtonProps = FloatingActionButtonDefinition

    val OutlinedButtonProps = OutlinedButtonDefinition

    val TextButtonProps = TextButtonDefinition
    val ColumnProps = ColumnDefinition
    val RowProps = RowDefinition
    val BoxProps = BoxDefinition
    val CardProps = CardDefinition
    val IconProps = IconDefinition
    val ImageProps = ImageDefinition
    val TextFieldProps = TextFieldDefinition
    val ScaffoldProps = ScaffoldDefinition
    val TopAppBarProps = TopAppBarDefinition

    val SpacerProps = SpacerDefinition

    val HorizontalDividerProps = HorizontalDividerDefinition

    val SwitchProps = SwitchDefinition

    val CheckboxProps = CheckboxDefinition

    val RadioButtonProps = RadioButtonDefinition

    val SliderProps = SliderDefinition

    val NavigationBarProps = NavigationBarDefinition
    val BadgeProps = BadgeDefinition
    val OutlinedTextFieldProps = OutlinedTextFieldDefinition

    val all: List<ComponentDefinition> = listOf(
        TextDefinition.definition,
        IconDefinition.definition,
        ImageDefinition.definition,
        ButtonDefinition.definition,
        IconButtonDefinition.definition,
        FloatingActionButtonDefinition.definition,
        OutlinedButtonDefinition.definition,
        TextButtonDefinition.definition,
        TextFieldDefinition.definition,
        ColumnDefinition.definition,
        RowDefinition.definition,
        BoxDefinition.definition,
        CardDefinition.definition,
        ScaffoldDefinition.definition,
        TopAppBarDefinition.definition,
        SpacerDefinition.definition,
        HorizontalDividerDefinition.definition,
        SwitchDefinition.definition,
        CheckboxDefinition.definition,
        RadioButtonDefinition.definition,
        SliderDefinition.definition,
        NavigationBarDefinition.definition,
        BadgeDefinition.definition,
        OutlinedTextFieldDefinition.definition,
    )

    private val byKind: Map<ComponentKind, ComponentDefinition> = all.associateBy { it.kind }

    /**
     * O(1) Non-nullable lookup by ComponentKind.
     */
    operator fun get(kind: ComponentKind): ComponentDefinition =
        byKind[kind] ?: error("No ComponentDefinition registered for $kind in ComponentCatalog")

    /**
     * Nullable lookup if probing for optional support.
     */
    fun find(kind: ComponentKind): ComponentDefinition? = byKind[kind]

    /**
     * Grouped by Material 3 category for palette presentation.
     */
    val groupedByCategory: Map<ComponentCategory, List<ComponentDefinition>> =
        all.groupBy { it.category }
}