package io.github.chandu4221.m3stage.port

import io.github.chandu4221.m3stage.model.Project

/**
 * Represents a single generated file.
 */
data class GeneratedFile(
    val packageName: String,
    val fileName: String,
    val content: String
)

/**
 * Represents the complete output of the code generation process.
 */
data class GeneratedProject(
    val files: List<GeneratedFile>
)

/**
 * Port for converting a project document into source code.
 */
interface CodeGenerator {
    fun generate(project: Project): GeneratedProject
}