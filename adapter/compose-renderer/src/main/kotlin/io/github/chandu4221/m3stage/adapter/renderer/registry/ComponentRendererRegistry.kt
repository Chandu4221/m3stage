package io.github.chandu4221.m3stage.adapter.renderer.registry

import io.github.chandu4221.m3stage.adapter.renderer.NodeRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.actions.ButtonRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.containment.*
import io.github.chandu4221.m3stage.adapter.renderer.component.layouts.BoxRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.layouts.ColumnRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.layouts.RowRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.navigation.TopAppBarRenderer
import io.github.chandu4221.m3stage.adapter.renderer.component.textinputs.TextFieldRenderer
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
        ComponentKind.TopAppBar to TopAppBarRenderer()
    )

    operator fun get(kind: ComponentKind): NodeRenderer? = registry[kind]
}
