package io.github.chandu4221.m3stage.adapter.codegen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import io.github.chandu4221.m3stage.component.ComposePackage

object ComposeSymbols {
    // Packages
    private const val UI_UNIT_PACKAGE = "androidx.compose.ui.unit"
    private const val RUNTIME_PACKAGE = "androidx.compose.runtime"
    private const val UI_GRAPHICS_PACKAGE = "androidx.compose.ui.graphics"
    private const val UI_TEXT_STYLE_PACKAGE = "androidx.compose.ui.text.style"
    private const val FOUNDATION_SHAPE_PACKAGE = "androidx.compose.foundation.shape"

    // Runtime Annotations
    val Composable = ClassName(RUNTIME_PACKAGE, "Composable")

    // UI Units, Graphics & Modifiers
    val Dp = MemberName(UI_UNIT_PACKAGE, "dp")
    val Modifier = ClassName(ComposePackage.Ui.packageName, "Modifier")
    val Color = ClassName(UI_GRAPHICS_PACKAGE, "Color")
    val Alignment = ClassName(ComposePackage.Ui.packageName, "Alignment")
    val Arrangement = ClassName(ComposePackage.FoundationLayout.packageName, "Arrangement")
    val RoundedCornerShape = ClassName(FOUNDATION_SHAPE_PACKAGE, "RoundedCornerShape")
    val TextOverflow = ClassName(UI_TEXT_STYLE_PACKAGE, "TextOverflow")

    // Material3 Theme & Defaults
    val MaterialTheme = ClassName(ComposePackage.Material3.packageName, "MaterialTheme")
    val CardDefaults = ClassName(ComposePackage.Material3.packageName, "CardDefaults")
    val ButtonDefaults = ClassName(ComposePackage.Material3.packageName, "ButtonDefaults")
    val TopAppBarDefaults = ClassName(ComposePackage.Material3.packageName, "TopAppBarDefaults")

    // Foundation Layout & Components
    val Box = ClassName(ComposePackage.FoundationLayout.packageName, "Box")
    val Text = ClassName(ComposePackage.Material3.packageName, "Text")
    val padding = MemberName(ComposePackage.FoundationLayout.packageName, "padding")
    val fillMaxSize = MemberName(ComposePackage.FoundationLayout.packageName, "fillMaxSize")
    val fillMaxWidth = MemberName(ComposePackage.FoundationLayout.packageName, "fillMaxWidth")
    val ExperimentalMaterial3Api = ClassName(ComposePackage.Material3.packageName, "ExperimentalMaterial3Api")
}