package io.github.chandu4221.m3stage.adapter.codegen

import com.facebook.ktfmt.format.Formatter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import io.github.chandu4221.m3stage.model.ComponentCatalog
import io.github.chandu4221.m3stage.model.ComponentCategory
import io.github.chandu4221.m3stage.model.ComponentType
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedFile
import io.github.chandu4221.m3stage.port.GeneratedProject
import kotlin.collections.mapNotNull

/**
 * Adapter implementation of CodeGenerator.
 * Uses ComponentCatalog for imports and ComponentCodegen registry for code generation.
 */
class ComposeCodeGenerator : CodeGenerator {

    // Registry mapping component types to their codegen implementations
    private val registry: Map<String, ComponentCodegen> = mapOf(
        "Text" to TextCodegen(),
        "Button" to ButtonCodegen(),
        "Column" to ColumnCodegen(),
        "Row" to RowCodegen(),
        "Box" to BoxCodegen(),
        "Card" to CardCodegen()
    )

    // Map categories to their import paths
    private val categoryImports: Map<ComponentCategory, List<ClassName>> = mapOf(
        ComponentCategory.LAYOUTS to listOf(
            ClassName("androidx.compose.foundation.layout", "Column"),
            ClassName("androidx.compose.foundation.layout", "Row"),
            ClassName("androidx.compose.foundation.layout", "Box"),
            ClassName("androidx.compose.foundation.layout", "Modifier"),
            ClassName("androidx.compose.foundation.layout", "fillMaxWidth"),
            ClassName("androidx.compose.foundation.layout", "padding")
        ),
        ComponentCategory.SURFACES to listOf(
            ClassName("androidx.compose.material3", "Card"),
            ClassName("androidx.compose.material3", "CardDefaults")
        ),
        ComponentCategory.INPUTS to listOf(
            ClassName("androidx.compose.material3", "Button"),
            ClassName("androidx.compose.material3", "TextField")
        ),
        ComponentCategory.DISPLAY to listOf(
            ClassName("androidx.compose.material3", "Text"),
            ClassName("androidx.compose.material3", "Icon")
        ),
        ComponentCategory.NAVIGATION to listOf(
            ClassName("androidx.compose.material3", "TopAppBar"),
            ClassName("androidx.compose.material3", "NavigationBar")
        )
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
        // Collect all component types used in this screen
        val usedTypes = collectUsedTypes(screen.root)

        // Determine which categories are used
        val usedCategories = usedTypes.mapNotNull { type ->
            ComponentCatalog.getByType(type)?.category
        }.toSet()

        // Collect all imports for used categories
        val imports = usedCategories.flatMap { category ->
            categoryImports[category] ?: emptyList()
        }

        // Recursive walker function
        fun walk(node: DesignNode): CodeBlock {
            val codegen = registry[node.type.value]
                ?: error("Unregistered component type: ${node.type.value}")
            return codegen.generate(node, ::walk)
        }

        val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")

        // Build the complete Kotlin file AST
        val fileSpecBuilder = FileSpec.builder(packageName, screen.name)
            .addFunction(
                FunSpec.builder("${screen.name}Screen")
                    .addAnnotation(composableAnnotation)
                    .addCode(walk(screen.root))
                    .build()
            )

        // Add all collected imports
        imports.forEach { import ->
            fileSpecBuilder.addImport(import.packageName, import.simpleName)
        }

        val fileSpec = fileSpecBuilder.build()
        val rawCode = fileSpec.toString()

        // Apply ktfmt to ensure the output matches standard Kotlin style
        return try {
            Formatter.format(Formatter.GOOGLE_FORMAT, rawCode)
        } catch (_: Exception) {
            rawCode
        }
    }

    /**
     * Recursively collect all component types used in a node tree.
     */
    private fun collectUsedTypes(node: DesignNode): Set<ComponentType> {
        val types = mutableSetOf(node.type)
        node.children.forEach { child ->
            types.addAll(collectUsedTypes(child))
        }
        return types
    }
}