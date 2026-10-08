package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.mutation.*
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findParent
import io.github.chandu4221.m3stage.query.findScreen

/**
 * Command pattern for editor actions.
 * Each command knows how to execute and undo itself on a Project.
 * No IdGenerator needed here because nodes are fully formed before execution.
 */
sealed interface EditorCommand {
    fun execute(project: Project): Project
    fun undo(project: Project): Project
}

/**
 * Adds a fully-formed node to a screen. Undo removes it.
 */
data class AddNodeCommand(
    val screenId: ScreenId,
    val parentId: NodeId,
    val newNode: DesignNode
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.addNode(parentId, newNode)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.removeNode(newNode.id)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Removes a node. Undo re-inserts it.
 * (Captures parent info at execution time for accurate undo).
 */
data class RemoveNodeCommand(
    val screenId: ScreenId,
    val nodeId: NodeId
) : EditorCommand {
    var removedNode: DesignNode? = null
    var parentId: NodeId? = null

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val nodeToRemove = screen.root.findNode(nodeId) ?: return project

        // Capture state for undo
        this.removedNode = nodeToRemove
        this.parentId = screen.root.findParent(nodeId)?.id ?: screen.root.id

        val newRoot = screen.root.removeNode(nodeId)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val node = removedNode ?: return project
        val parent = parentId ?: return project

        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.addNode(parent, node)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Updates a property. Undo restores the old value or removes the key.
 * Uses PropertyId and PropertyValue to avoid generic type erasure in the undo stack.
 */
data class UpdatePropCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val propId: PropertyId,
    val oldValue: PropertyValue?,
    val newValue: PropertyValue
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.updatePropById(nodeId, propId, newValue)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = if (oldValue == null) {
            screen.root.removeProp(nodeId, propId)
        } else {
            screen.root.updatePropById(nodeId, propId, oldValue)
        }
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Toggles the visibility of a node. Undo restores the previous visibility state.
 */
data class UpdateVisibilityCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val oldVisibility: Boolean,
    val newVisibility: Boolean
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.updateVisibility(nodeId, newVisibility)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.updateVisibility(nodeId, oldVisibility)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Adds a new screen to the project. Undo removes it.
 */
data class AddScreenCommand(
    val newScreen: Screen
) : EditorCommand {
    override fun execute(project: Project): Project {
        return project.copy(screens = project.screens + newScreen)
    }

    override fun undo(project: Project): Project {
        return project.copy(screens = project.screens.filter { it.id != newScreen.id })
    }
}