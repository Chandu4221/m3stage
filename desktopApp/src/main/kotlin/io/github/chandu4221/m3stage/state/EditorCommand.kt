package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.IdGenerator

/**
 * Command pattern for editor actions.
 * Each command knows how to execute and undo itself.
 */
sealed interface EditorCommand {
    fun execute(project: Project, idGenerator: IdGenerator): Project
    fun undo(project: Project): Project
}

/**
 * Adds a node to a screen. Undo removes it.
 */
data class AddNodeCommand(
    val screenId: ScreenId,
    val parentId: NodeId,
    val newNode: DesignNode
) : EditorCommand {

    override fun execute(project: Project, idGenerator: IdGenerator): Project {
        return project.updateScreen(screenId) { screen ->
            screen.copy(root = screen.root.addNode(parentId, newNode))
        }
    }

    override fun undo(project: Project): Project {
        return project.updateScreen(screenId) { screen ->
            screen.copy(root = screen.root.removeNode(newNode.id))
        }
    }
}

/**
 * Removes a node. Undo re-inserts it at its original position.
 */
data class RemoveNodeCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val removedNode: DesignNode,
    val parentId: NodeId,
    val indexInParent: Int
) : EditorCommand {

    override fun execute(project: Project, idGenerator: IdGenerator): Project {
        return project.updateScreen(screenId) { screen ->
            screen.copy(root = screen.root.removeNode(nodeId))
        }
    }

    override fun undo(project: Project): Project {
        return project.updateScreen(screenId) { screen ->
            screen.copy(root = screen.root.addNode(parentId, removedNode))
        }
    }
}

/**
 * Updates a property. Undo restores the old value.
 */
data class UpdatePropCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val key: String,
    val oldValue: String?,
    val newValue: String
) : EditorCommand {

    override fun execute(project: Project, idGenerator: IdGenerator): Project {
        return project.updateScreen(screenId) { screen ->
            screen.copy(root = screen.root.updateProp(nodeId, key, newValue))
        }
    }

    override fun undo(project: Project): Project {
        return project.updateScreen(screenId) { screen ->
            val updatedRoot = if (oldValue == null) {
                screen.root.removeProp(nodeId, key)
            } else {
                screen.root.updateProp(nodeId, key, oldValue)
            }
            screen.copy(root = updatedRoot)
        }
    }
}