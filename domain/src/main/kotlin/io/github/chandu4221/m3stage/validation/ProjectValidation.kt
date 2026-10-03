package io.github.chandu4221.m3stage.validation

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project

/**
 * Validates project invariants.
 * Returns a list of error messages. If empty, the project is valid.
 */
object ProjectValidation {

    fun validate(project: Project): List<String> {
        val errors = mutableListOf<String>()

        if (project.screens.isEmpty()) {
            errors.add("Project must contain at least one screen.")
        }

        val seenNodeIds = mutableSetOf<String>()

        fun checkDuplicates(node: DesignNode, path: String) {
            if (!seenNodeIds.add(node.id.value)) {
                errors.add("Duplicate Node ID detected: '${node.id.value}' at $path")
            }

            node.children.forEach { child ->
                checkDuplicates(child, "$path -> ${child.type.value}")
            }
        }

        project.screens.forEach { screen ->
            checkDuplicates(screen.root, "Screen '${screen.name}'")
        }

        return errors
    }
}