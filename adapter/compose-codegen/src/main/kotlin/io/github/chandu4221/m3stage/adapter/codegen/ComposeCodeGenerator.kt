package io.github.chandu4221.m3stage.adapter.codegen

import com.facebook.ktfmt.format.Formatter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
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
    private val registry: Map<String, ComponentCodegen> = mapOf(
        "Text" to TextCodegen(),
        "Button" to ButtonCodegen(),
        "Column" to ColumnCodegen(),
        "Row" to RowCodegen(),
        "Box" to BoxCodegen(),
        "Card" to CardCodegen()
    )

    override fun generate(project: Project): GeneratedProject {
        val files = project.screens.map { screen ->
            val content = exportScreen(screen, project.basePackage)
            GeneratedFile(
                packageName = project.basePackage,
                fileName = "${screen.name}.kt",
                content = content
            )
        }
        return GeneratedProject(files)
    }

    private fun exportScreen(screen: Screen, packageName: String): String {
        // 1. Collect all unique ClassNames used in this screen
        val usedClassNames = collectUsedClassNames(screen.root)

        // 2. Recursive walker
        fun walk(node: DesignNode): CodeBlock {
            if (!node.isVisible) return CodeBlock.builder().build()

            val codegen = registry[node.type.value]
                ?: error("Unregistered component type: ${node.type.value}")
            return codegen.generate(node, ::walk)
        }

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")

        // 3. Build the FileSpec
        val fileSpecBuilder = FileSpec.builder(packageName, screen.name)
            .addFunction(
                FunSpec.builder("${screen.name}Screen")
                    .addAnnotation(composableAnnotation)
                    .addCode(walk(screen.root))
                    .build()
            )

        // 4. Add imports directly from the collected ClassNames
        usedClassNames.forEach { className ->
            fileSpecBuilder.addImport(className.packageName, className.simpleName)
        }

        val fileSpec = fileSpecBuilder.build()
        val rawCode = fileSpec.toString()

        // 5. Format with ktfmt
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

        val codegen = registry[node.type.value]
        if (codegen != null) {
            result.add(codegen.className)
        }
        node.children.forEach { child ->
            collectUsedClassNamesRecursive(child, result)
        }
    }
}