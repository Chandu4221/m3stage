package io.github.chandu4221.m3stage.adapter.codegen

import com.facebook.ktfmt.format.Formatter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import io.github.chandu4221.m3stage.adapter.codegen.component.BoxCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.ButtonCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.CardCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.ColumnCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.RowCodegen
import io.github.chandu4221.m3stage.adapter.codegen.component.TextCodegen
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
        ComponentKind.Button to ButtonCodegen(),
        ComponentKind.Column to ColumnCodegen(),
        ComponentKind.Row to RowCodegen(),
        ComponentKind.Box to BoxCodegen(),
        ComponentKind.Card to CardCodegen()
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
        val usedClassNames = collectUsedClassNames(screen.root)
        fun walk(node: DesignNode): CodeBlock {
            if (!node.isVisible) return CodeBlock.builder().build()
            val codegen = registry[node.kind]
                ?: error("Unregistered component kind: ${node.kind}")
            return codegen.generate(node, ::walk)
        }

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
        val fileSpecBuilder = FileSpec.builder(packageName, screen.name)
            .addFunction(
                FunSpec.builder("${screen.name}Screen")
                    .addAnnotation(composableAnnotation)
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