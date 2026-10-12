package io.github.chandu4221.m3stage.adapter.renderer.registry

import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.actions.*
import io.github.chandu4221.m3stage.adapter.renderer.component.containment.*
import io.github.chandu4221.m3stage.adapter.renderer.component.layouts.*
import io.github.chandu4221.m3stage.adapter.renderer.component.navigation.*
import io.github.chandu4221.m3stage.adapter.renderer.component.textinputs.*
import io.github.chandu4221.m3stage.component.ComponentKind

object ComponentRendererRegistry {

    private val registry: Map<ComponentKind, NodeRenderer> = mapOf(
        ComponentKind.Text to TextRenderer(),
        ComponentKind.Icon to IconRenderer(),
        ComponentKind.Image to ImageRenderer(),
        ComponentKind.Button to ButtonRenderer(),
        ComponentKind.TextField to TextFieldRenderer(),
        ComponentKind.Column to ColumnRenderer(),
        ComponentKind.Row to RowRenderer(),
        ComponentKind.Box to BoxRenderer(),
        ComponentKind.Card to CardRenderer(),
        ComponentKind.Scaffold to ScaffoldRenderer(),
        ComponentKind.TopAppBar to TopAppBarRenderer(),
        // 13 new components
        ComponentKind.Badge to BadgeRenderer(),
        ComponentKind.Checkbox to CheckboxRenderer(),
        ComponentKind.Switch to SwitchRenderer(),
        ComponentKind.Slider to SliderRenderer(),
        ComponentKind.Spacer to SpacerRenderer(),
        ComponentKind.HorizontalDivider to HorizontalDividerRenderer(),
        ComponentKind.FloatingActionButton to FloatingActionButtonRenderer(),
        ComponentKind.IconButton to IconButtonRenderer(),
        ComponentKind.NavigationBar to NavigationBarRenderer(),
        ComponentKind.RadioButton to RadioButtonRenderer(),
        ComponentKind.OutlinedButton to OutlinedButtonRenderer(),
        ComponentKind.OutlinedTextField to OutlinedTextFieldRenderer(),
        ComponentKind.TextButton to TextButtonRenderer()
    )

    val registeredKinds: Set<ComponentKind> = registry.keys

    operator fun get(kind: ComponentKind): NodeRenderer? = registry[kind]
}
