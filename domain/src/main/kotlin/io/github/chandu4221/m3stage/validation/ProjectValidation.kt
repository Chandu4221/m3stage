package io.github.chandu4221.m3stage.validation

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project

/**
 * Validates project and AST invariants.
 * Returns a list of human-readable error messages. If empty, the project is valid.
 */
object ProjectValidation {

    fun validate(project: Project): List<String> {
        val errors = mutableListOf<String>()

        if (project.screens.isEmpty()) {
            errors.add("Project must contain at least one screen.")
        }

        val seenNodeIds = mutableSetOf<String>()

        fun validateSubtree(node: DesignNode, path: String) {
            // 1. Node ID Uniqueness
            if (!seenNodeIds.add(node.id.value)) {
                errors.add("Duplicate Node ID detected: '${node.id.value}' at $path")
            }

            val def = ComponentCatalog.find(node.kind)
            if (def == null) {
                errors.add("Unregistered component kind '${node.kind}' at $path")
                return
            }

            // 2. Non-container rule
            if (!def.isContainer && node.allDirectChildren.isNotEmpty()) {
                errors.add("Component '${node.kind.displayName}' at $path is a leaf node and cannot contain children.")
            }

            // 3. Raw children atomic validation
            node.children.forEach { child ->
                val childDef = ComponentCatalog.find(child.kind)
                if (childDef?.slotOnly == true) {
                    errors.add("Component '${child.kind.displayName}' at $path/${child.kind.displayName} is slot-only and cannot be placed directly in children.")
                } else if (!def.canAcceptChild(child.kind)) {
                    errors.add("Component '${node.kind.displayName}' at $path cannot accept child of kind '${child.kind.displayName}'.")
                }
            }

            // 4. Slot validation (allowed kinds and max children)
            node.slots.forEach { (slotId, slotChildren) ->
                val slotDef = def.findSlot(slotId)
                if (slotDef == null) {
                    errors.add("Unknown slot '${slotId.value}' on component '${node.kind.displayName}' at $path.")
                } else {
                    if (slotDef.maxChildren != null && slotChildren.size > slotDef.maxChildren) {
                        errors.add("Slot '${slotDef.displayName}' on '${node.kind.displayName}' at $path exceeds maximum capacity of ${slotDef.maxChildren} (found ${slotChildren.size}).")
                    }
                    slotChildren.forEach { child ->
                        if (!slotDef.canAccept(child.kind)) {
                            errors.add("Slot '${slotDef.displayName}' on '${node.kind.displayName}' at $path cannot accept child of kind '${child.kind.displayName}'.")
                        }
                    }
                }
            }

            // 5. Dual-containment invariant rule (INV-4)
            val childrenIds = node.children.map { it.id.value }.toSet()
            node.slots.forEach { (slotId, slotChildren) ->
                slotChildren.forEach { slotChild ->
                    if (slotChild.id.value in childrenIds) {
                        errors.add("Node '${slotChild.kind}' (id=${slotChild.id.value}) is contained in both children and slot '${slotId.value}' of parent '${node.kind}'")
                    }
                }
            }

            // 5. Recursively validate all descendants
            node.allDirectChildren.forEach { child ->
                validateSubtree(child, "$path -> ${child.kind.displayName}")
            }
        }

        project.screens.forEach { screen ->
            validateSubtree(screen.root, "Screen '${screen.name}'")
        }

        return errors
    }
}