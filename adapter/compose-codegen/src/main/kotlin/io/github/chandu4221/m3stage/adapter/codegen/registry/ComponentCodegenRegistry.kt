package io.github.chandu4221.m3stage.adapter.codegen.registry

import io.github.chandu4221.m3stage.adapter.codegen.ComponentCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.actions.*
import io.github.chandu4221.m3stage.adapter.codegen.component.containment.*
import io.github.chandu4221.m3stage.adapter.codegen.component.layouts.*
import io.github.chandu4221.m3stage.adapter.codegen.component.navigation.*
import io.github.chandu4221.m3stage.adapter.codegen.component.textinputs.*
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
        ComponentKind.TopAppBar to TopAppBarCodegen(),
        // 13 new components
        ComponentKind.Badge to BadgeCodegen(),
        ComponentKind.Checkbox to CheckboxCodegen(),
        ComponentKind.Switch to SwitchCodegen(),
        ComponentKind.Slider to SliderCodegen(),
        ComponentKind.Spacer to SpacerCodegen(),
        ComponentKind.HorizontalDivider to HorizontalDividerCodegen(),
        ComponentKind.FloatingActionButton to FloatingActionButtonCodegen(),
        ComponentKind.IconButton to IconButtonCodegen(),
        ComponentKind.NavigationBar to NavigationBarCodegen(),
        ComponentKind.NavigationBarItem to NavigationBarItemCodegen(),
        ComponentKind.RadioButton to RadioButtonCodegen(),
        ComponentKind.OutlinedButton to OutlinedButtonCodegen(),
        ComponentKind.OutlinedTextField to OutlinedTextFieldCodegen(),
        ComponentKind.TextButton to TextButtonCodegen()
    )

    val registeredKinds: Set<ComponentKind> = registry.keys

    operator fun get(kind: ComponentKind): ComponentCodegen? = registry[kind]
}
