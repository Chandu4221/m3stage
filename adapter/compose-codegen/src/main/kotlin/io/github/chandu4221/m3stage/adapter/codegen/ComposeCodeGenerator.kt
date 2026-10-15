package io.github.chandu4221.m3stage.adapter.codegen

import com.facebook.ktfmt.format.Formatter
import com.squareup.kotlinpoet.*
import io.github.chandu4221.m3stage.adapter.codegen.registry.ComponentCodegenRegistry
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ComposeSymbols
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver
import io.github.chandu4221.m3stage.adapter.codegen.resolver.ModifierCodeResolver.requiredImport
import io.github.chandu4221.m3stage.adapter.codegen.util.KotlinIdentifierSanitizer
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedFile
import io.github.chandu4221.m3stage.port.GeneratedProject

/**
 * Adapter implementation of CodeGenerator.
 * Uses ComponentCodegenRegistry and ComposeSymbols for idiomatic Jetpack Compose code generation.
 */
class ComposeCodeGenerator : CodeGenerator {

    override fun generate(project: Project): GeneratedProject {
        val seenNames = mutableMapOf<String, Int>()

        val files = project.screens.map { screen ->
            val baseName = KotlinIdentifierSanitizer.toPascalCase(screen.name)
            val count = seenNames.getOrDefault(baseName, 0)
            seenNames[baseName] = count + 1
            val sanitizedName = if (count == 0) baseName else "${baseName}_$count"

            val content = exportScreen(screen, sanitizedName, project.basePackage)
            GeneratedFile(
                packageName = project.basePackage,
                fileName = "$sanitizedName.kt",
                content = content
            )
        }
        return GeneratedProject(files)
    }

    private fun exportScreen(screen: Screen, screenIdentifier: String, packageName: String): String {
        return io.github.chandu4221.m3stage.adapter.codegen.resolver.AdaptiveCodegenContext.withAdaptive(screen.adaptive) {
            val usedClassNames = collectUsedClassNames(screen.root, screen.adaptive)
            val parentKindMap = mutableMapOf<io.github.chandu4221.m3stage.model.NodeId, io.github.chandu4221.m3stage.component.ComponentKind>()
            fun buildParentMap(node: DesignNode) {
                node.allDirectChildren.forEach { child ->
                    parentKindMap[child.id] = node.kind
                    buildParentMap(child)
                }
            }
            buildParentMap(screen.root)

            fun walk(node: DesignNode): CodeBlock {
                if (!node.isVisible) return CodeBlock.builder().build()
                val codegen = ComponentCodegenRegistry.get(node.kind)
                    ?: return CodeBlock.of("// TODO: Unsupported component kind %L\n", node.kind)
                return ModifierCodeResolver.withParentKind(parentKindMap[node.id]) {
                    codegen.generate(node, ::walk)
                }
            }

            val optInAnnotation = AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
                .addMember("%T::class", ComposeSymbols.ExperimentalMaterial3Api)
                .build()

            val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
            val functionName = KotlinIdentifierSanitizer.toFunctionName(screenIdentifier)

            val functionBuilder = FunSpec.builder(functionName)
                .addAnnotation(composableAnnotation)
                .addAnnotation(optInAnnotation)

            val fileSpecBuilder = FileSpec.builder(packageName, screenIdentifier)

            if (screen.adaptive) {
                val windowSizeClassType = ClassName("io.github.chandu4221.m3stage.model", "WindowSizeClass")
                functionBuilder.addParameter(
                    ParameterSpec.builder("windowSizeClass", windowSizeClassType)
                        .defaultValue("%T.Compact", windowSizeClassType)
                        .build()
                )
                fileSpecBuilder.addImport("io.github.chandu4221.m3stage.model", "WindowSizeClass")
            }

            functionBuilder.addCode(walk(screen.root))
            fileSpecBuilder.addFunction(functionBuilder.build())

            usedClassNames.forEach { className ->
                fileSpecBuilder.addImport(className.packageName, className.simpleName)
            }

            val usedModifiers = collectUsedModifiers(screen.root)
            if (usedModifiers.isNotEmpty()) {
                fileSpecBuilder.addImport("androidx.compose.ui", "Modifier")
                val modifierPackages = usedModifiers.mapNotNull { it.requiredImport() }.toSortedSet()
                for (pkg in modifierPackages) {
                when (pkg) {
                    "androidx.compose.ui" -> {
                        fileSpecBuilder.addImport("androidx.compose.ui", "zIndex")
                    }
                    "androidx.compose.foundation.layout" -> {
                        fileSpecBuilder.addImport(
                            "androidx.compose.foundation.layout",
                            "fillMaxSize",
                            "fillMaxWidth",
                            "fillMaxHeight",
                            "padding",
                            "size",
                            "width",
                            "height",
                            "wrapContentSize",
                            "offset",
                            "weight",
                            "aspectRatio"
                        )
                    }
                    "androidx.compose.foundation" -> {
                        fileSpecBuilder.addImport(
                            "androidx.compose.foundation",
                            "background",
                            "border",
                            "verticalScroll",
                            "horizontalScroll",
                            "rememberScrollState"
                        )
                    }
                    "androidx.compose.ui.draw" -> {
                        fileSpecBuilder.addImport(
                            "androidx.compose.ui.draw",
                            "clip",
                            "shadow",
                            "alpha",
                            "rotate",
                            "scale"
                        )
                    }
                }
            }
            fileSpecBuilder.addImport("androidx.compose.ui.unit", "dp")
        }

        val fileSpec = fileSpecBuilder.build()
        val rawCode = fileSpec.toString()
        try {
            Formatter.format(Formatter.GOOGLE_FORMAT, rawCode)
        } catch (e: Exception) {
            System.err.println("ktfmt formatting fallback for screen '$screenIdentifier': ${e.message}")
            rawCode
        }
    }
}

    private fun collectUsedClassNames(node: DesignNode, isAdaptive: Boolean = false): Set<ClassName> {
        val result = mutableSetOf<ClassName>()
        collectUsedClassNamesRecursive(node, result, isAdaptive)
        return result
    }

    private fun collectUsedClassNamesRecursive(node: DesignNode, result: MutableSet<ClassName>, isAdaptive: Boolean) {
        if (!node.isVisible) return
        val codegen = ComponentCodegenRegistry.get(node.kind)
        if (codegen != null) {
            result.add(codegen.className)
            if (isAdaptive && node.kind == io.github.chandu4221.m3stage.component.ComponentKind.NavigationBar) {
                result.add(ClassName("androidx.compose.material3", "NavigationRail"))
                result.add(ClassName("androidx.compose.material3", "NavigationRailItem"))
                result.add(ClassName("io.github.chandu4221.m3stage.model", "WindowSizeClass"))
            }
        }
        node.allDirectChildren.forEach { child ->
            collectUsedClassNamesRecursive(child, result, isAdaptive)
        }
    }

    private fun collectUsedModifiers(node: DesignNode): Set<io.github.chandu4221.m3stage.modifier.ModifierSpec> {
        val result = mutableSetOf<io.github.chandu4221.m3stage.modifier.ModifierSpec>()
        collectUsedModifiersRecursive(node, result)
        return result
    }

    private fun collectUsedModifiersRecursive(node: DesignNode, result: MutableSet<io.github.chandu4221.m3stage.modifier.ModifierSpec>) {
        if (!node.isVisible) return
        result.addAll(node.modifiers)
        node.allDirectChildren.forEach { child ->
            collectUsedModifiersRecursive(child, result)
        }
    }
}
