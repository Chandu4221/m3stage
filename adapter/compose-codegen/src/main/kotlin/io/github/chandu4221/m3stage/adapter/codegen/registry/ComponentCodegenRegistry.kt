package io.github.chandu4221.m3stage.adapter.codegen.registry

import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.actions.ButtonCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.containment.*
import io.github.chandu4221.m3stage.adapter.codegen.component.layouts.BoxCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.layouts.ColumnCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.layouts.RowCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.navigation.TopAppBarCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.textinputs.TextFieldCodegen
import io.github.chandu4221.m3stage.component.ComponentKind

object ComponentCodegenRegistry {

    private val registry: Map<ComponentKind, ComponentCodegen> = mapOf(
        ComponentKind.Text to TextCodegen(),
        ComponentKind.Icon to IconCodegen(),
        ComponentKind.Image to ImageCodegen(),
        ComponentKind.Button to ButtonCodegen(),
        ComponentKind.TextField to TextFieldCodegen(),
        ComponentKind.Column to ColumnCodegen(),
        ComponentKind.Row to RowCodegen(),
        ComponentKind.Box to BoxCodegen(),
        ComponentKind.Card to CardCodegen(),
        ComponentKind.Scaffold to ScaffoldCodegen(),
        ComponentKind.TopAppBar to TopAppBarCodegen()
    )

    operator fun get(kind: ComponentKind): ComponentCodegen? = registry[kind]
}
