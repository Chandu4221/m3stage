package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import io.github.chandu4221.m3stage.component.ComposeModule

object ComposeSymbols {
    // Packages
    private const val UI_UNIT_PACKAGE = "androidx.compose.ui.unit"
    private const val RUNTIME_PACKAGE = "androidx.compose.runtime"

    // Runtime Annotations
    val Composable = ClassName(RUNTIME_PACKAGE, "Composable")

    // UI Units & Modifiers
    val Dp = MemberName(UI_UNIT_PACKAGE, "dp")
    val Modifier = ClassName(ComposeModule.Ui.packageName, "Modifier")

    // Component Defaults
    val CardDefaults = ClassName(ComposeModule.Material3.packageName, "CardDefaults")
    val ButtonDefaults = ClassName(ComposeModule.Material3.packageName, "ButtonDefaults")
}