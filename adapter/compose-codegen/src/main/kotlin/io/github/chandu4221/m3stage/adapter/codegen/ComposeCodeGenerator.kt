package io.github.chandu4221.m3stage.adapter.codegen

import com.facebook.ktfmt.format.Formatter
import com.squareup.kotlinpoet.*
import io.github.chandu4221.m3stage.adapter.codegen.component.*
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedFile
import io.github.chandu4221.m3stage.port.GeneratedProject

/**
 * Adapter implementation of CodeGenerator.
 * Uses ComponentCatalog for imports and ComponentCodegen registry for code generation.
 */
class ComposeCodeGenerator : CodeGenerator {

    // Registry mapping component types to their codegen implementations.
    // Each codegen internally uses PackageNameResolver for its ClassName.
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
        val usedClassNames = collectUsedClassNames(screen.root)
        fun walk(node: DesignNode): CodeBlock {
            if (!node.isVisible) return CodeBlock.builder().build()
            val codegen = registry[node.kind]
                ?: return CodeBlock.of("// TODO: Unsupported component kind %L\n", node.kind)
            return codegen.generate(node, ::walk)
        }

        val optInAnnotation = AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
            .addMember("%T::class", ComposeSymbols.ExperimentalMaterial3Api)
            .build()

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val functionName = KotlinIdentifierSanitizer.toFunctionName(screenIdentifier)

        val fileSpecBuilder = FileSpec.builder(packageName, screenIdentifier)
            .addFunction(
                FunSpec.builder(functionName)
                    .addAnnotation(composableAnnotation)
                    .addAnnotation(optInAnnotation)
                    .addCode(walk(screen.root))
                    .build()
            )
        usedClassNames.forEach { className ->
            fileSpecBuilder.addImport(className.packageName, className.simpleName)
        }
        val fileSpec = fileSpecBuilder.build()
        val rawCode = fileSpec.toString()
        return try {
            Formatter.format(Formatter.GOOGLE_FORMAT, rawCode)
        } catch (_: Exception) {
            rawCode
        }
    }

    private fun collectUsedClassNames(node: DesignNode): Set<ClassName> {
        val result = mutableSetOf<ClassName>()
        collectUsedClassNamesRecursive(node, result)
        return result
    }

    private fun collectUsedClassNamesRecursive(node: DesignNode, result: MutableSet<ClassName>) {
        if (!node.isVisible) return
        val codegen = registry[node.kind]
        if (codegen != null) {
            result.add(codegen.className)
        }
        node.children.forEach { child ->
            collectUsedClassNamesRecursive(child, result)
        }
    }
}