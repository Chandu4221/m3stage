package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.modifier.ModifierSpec
import io.github.chandu4221.m3stage.mutation.*
import io.github.chandu4221.m3stage.property.PropertyId
import io.github.chandu4221.m3stage.property.PropertyValue
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.findParent
import io.github.chandu4221.m3stage.query.findScreen
import io.github.chandu4221.m3stage.query.isDescendantOf

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
    val newNode: DesignNode,
    val slotId: io.github.chandu4221.m3stage.component.slot.SlotId? = null
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.addNode(parentId, newNode, slotId)
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
 * (Captures parent and slot info at execution time for accurate undo).
 */
data class RemoveNodeCommand(
    val screenId: ScreenId,
    val nodeId: NodeId
) : EditorCommand {
    var removedNode: DesignNode? = null
    var parentId: NodeId? = null
    var slotId: SlotId? = null
    var index: Int = -1

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val nodeToRemove = screen.root.findNode(nodeId) ?: return project
        val parent = screen.root.findParent(nodeId)

        // Capture state for undo
        this.removedNode = nodeToRemove
        this.parentId = parent?.id
        if (parent != null) {
            val foundSlot = parent.findSlotForNode(nodeId)
            this.slotId = foundSlot
            if (foundSlot != null) {
                this.index = (parent.slots[foundSlot] ?: emptyList()).indexOfFirst { it.id == nodeId }
            } else {
                this.index = parent.children.indexOfFirst { it.id == nodeId }
            }
        }

        val newRoot = screen.root.removeNode(nodeId)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val node = removedNode ?: return project
        val parent = parentId ?: return project

        val screen = project.findScreen(screenId) ?: return project
        val newRoot = if (index >= 0) {
            screen.root.insertNode(parent, node, index, slotId)
        } else {
            screen.root.addNode(parent, node, slotId)
        }
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
    private var oldPrefilled: Boolean? = null

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val targetNode = screen.root.findNode(nodeId) ?: return project
        if (oldPrefilled == null) {
            oldPrefilled = targetNode.isPrefilled
        }
        val newRoot = screen.root.updatePropById(nodeId, propId, newValue, clearPrefilled = true)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        var newRoot = if (oldValue == null) {
            screen.root.removeProp(nodeId, propId)
        } else {
            screen.root.updatePropById(nodeId, propId, oldValue, clearPrefilled = false)
        }
        oldPrefilled?.let {
            newRoot = newRoot.updatePrefilled(nodeId, it)
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

/**
 * Renames a screen. Undo restores previous name.
 */
data class RenameScreenCommand(
    val screenId: ScreenId,
    val oldName: String,
    val newName: String
) : EditorCommand {
    override fun execute(project: Project): Project {
        val updatedScreens = project.screens.map { screen ->
            if (screen.id == screenId) screen.copy(name = newName) else screen
        }
        return project.copy(screens = updatedScreens)
    }

    override fun undo(project: Project): Project {
        val updatedScreens = project.screens.map { screen ->
            if (screen.id == screenId) screen.copy(name = oldName) else screen
        }
        return project.copy(screens = updatedScreens)
    }
}

/**
 * Deletes a screen at oldIndex. Undo re-inserts it at oldIndex.
 */
data class DeleteScreenCommand(
    val screen: Screen,
    val oldIndex: Int
) : EditorCommand {
    override fun execute(project: Project): Project {
        require(project.screens.size > 1) { "Cannot delete the last screen in project." }
        val updatedScreens = project.screens.filter { it.id != screen.id }
        return project.copy(screens = updatedScreens)
    }

    override fun undo(project: Project): Project {
        val mutable = project.screens.toMutableList()
        val safeIndex = oldIndex.coerceIn(0, mutable.size)
        mutable.add(safeIndex, screen)
        return project.copy(screens = mutable)
    }
}

/**
 * Duplicates a screen with a fresh screen ID and fresh node IDs. Undo removes it.
 */
data class DuplicateScreenCommand(
    val newScreen: Screen
) : EditorCommand {
    override fun execute(project: Project): Project {
        return project.copy(screens = project.screens + newScreen)
    }

    override fun undo(project: Project): Project {
        return project.copy(screens = project.screens.filter { it.id != newScreen.id })
    }
}

/**
 * Reorders screens by moving screen from fromIndex to toIndex. Undo moves back using identity.
 */
data class ReorderScreensCommand(
    val fromIndex: Int,
    val toIndex: Int
) : EditorCommand {
    var movedScreenId: ScreenId? = null
    var originalIndex: Int = fromIndex

    override fun execute(project: Project): Project {
        if (fromIndex !in project.screens.indices || toIndex !in project.screens.indices) return project
        val screen = project.screens[fromIndex]
        this.movedScreenId = screen.id
        this.originalIndex = fromIndex
        val mutable = project.screens.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        return project.copy(screens = mutable)
    }

    override fun undo(project: Project): Project {
        val screenId = movedScreenId
        if (screenId != null) {
            val currentIndex = project.screens.indexOfFirst { it.id == screenId }
            if (currentIndex >= 0) {
                val mutable = project.screens.toMutableList()
                val item = mutable.removeAt(currentIndex)
                val targetIndex = originalIndex.coerceIn(0, mutable.size)
                mutable.add(targetIndex, item)
                return project.copy(screens = mutable)
            }
        }
        if (toIndex !in project.screens.indices) return project
        val mutable = project.screens.toMutableList()
        val item = mutable.removeAt(toIndex)
        val targetIndex = fromIndex.coerceIn(0, mutable.size)
        mutable.add(targetIndex, item)
        return project.copy(screens = mutable)
    }
}

/**
 * Moves a screen to a new canvas coordinate (x, y). Undo restores previous coordinate.
 */
data class MoveScreenCommand(
    val screenId: ScreenId,
    val oldX: Float,
    val oldY: Float,
    val newX: Float,
    val newY: Float
) : EditorCommand {
    constructor(screenId: ScreenId, newX: Float, newY: Float) : this(screenId, 0f, 0f, newX, newY)

    override fun execute(project: Project): Project {
        val updatedScreens = project.screens.map {
            if (it.id == screenId) it.copy(canvasX = newX, canvasY = newY) else it
        }
        return project.copy(screens = updatedScreens)
    }

    override fun undo(project: Project): Project {
        val updatedScreens = project.screens.map {
            if (it.id == screenId) it.copy(canvasX = oldX, canvasY = oldY) else it
        }
        return project.copy(screens = updatedScreens)
    }
}

/**
 * Tidies all screens along a clean horizontal row. Undo restores previous coordinates for all screens.
 */
data class TidyScreensCommand(
    val oldPositions: Map<ScreenId, Pair<Float, Float>>,
    val newPositions: Map<ScreenId, Pair<Float, Float>>
) : EditorCommand {
    constructor(newPositions: Map<ScreenId, Pair<Float, Float>>) : this(emptyMap(), newPositions)

    override fun execute(project: Project): Project {
        val updatedScreens = project.screens.map { screen ->
            val pos = newPositions[screen.id]
            if (pos != null) screen.copy(canvasX = pos.first, canvasY = pos.second) else screen
        }
        return project.copy(screens = updatedScreens)
    }

    override fun undo(project: Project): Project {
        val updatedScreens = project.screens.map { screen ->
            val pos = oldPositions[screen.id]
            if (pos != null) screen.copy(canvasX = pos.first, canvasY = pos.second) else screen
        }
        return project.copy(screens = updatedScreens)
    }
}


/**
 * Updates the project's seed color and dark mode theme. Undo restores the previous theme.
 */
data class UpdateThemeCommand(
    val oldSeedColor: Long,
    val oldIsDarkMode: Boolean,
    val newSeedColor: Long,
    val newIsDarkMode: Boolean
) : EditorCommand {
    override fun execute(project: Project): Project {
        return project.copy(seedColor = newSeedColor, isDarkMode = newIsDarkMode)
    }

    override fun undo(project: Project): Project {
        return project.copy(seedColor = oldSeedColor, isDarkMode = oldIsDarkMode)
    }
}

/**
 * Updates the project's name and base package. Undo restores previous metadata.
 */
data class UpdateProjectMetaCommand(
    val oldName: String,
    val oldBasePackage: String,
    val newName: String,
    val newBasePackage: String
) : EditorCommand {
    override fun execute(project: Project): Project {
        return project.updateMetadata(newName, newBasePackage)
    }

    override fun undo(project: Project): Project {
        return project.updateMetadata(oldName, oldBasePackage)
    }
}

/**
 * Updates a screen's device preset and orientation metadata. Undo restores previous metadata.
 */
data class UpdateScreenMetaCommand(
    val screenId: ScreenId,
    val oldDevice: DevicePreset?,
    val oldOrientation: DeviceOrientation,
    val newDevice: DevicePreset?,
    val newOrientation: DeviceOrientation
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val updatedScreen = screen.copy(device = newDevice, orientation = newOrientation)
        return project.copy(screens = project.screens.map { if (it.id == screenId) updatedScreen else it })
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val revertedScreen = screen.copy(device = oldDevice, orientation = oldOrientation)
        return project.copy(screens = project.screens.map { if (it.id == screenId) revertedScreen else it })
    }
}


/**
 * Adds a modifier to a node. Undo removes it.
 */
data class AddModifierCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val modifier: ModifierSpec
) : EditorCommand {
    private var addedIndex: Int = -1

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val targetNode = screen.root.findNode(nodeId) ?: return project
        addedIndex = targetNode.modifiers.size
        val newRoot = screen.root.addModifier(nodeId, modifier)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        if (addedIndex < 0) return project
        val newRoot = screen.root.removeModifier(nodeId, addedIndex)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Removes a modifier from a node at an index. Undo re-inserts it.
 */
data class RemoveModifierCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val index: Int
) : EditorCommand {
    private var removedModifier: ModifierSpec? = null

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val targetNode = screen.root.findNode(nodeId) ?: return project
        if (index !in targetNode.modifiers.indices) return project
        removedModifier = targetNode.modifiers[index]
        val newRoot = screen.root.removeModifier(nodeId, index)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val mod = removedModifier ?: return project
        val targetNode = screen.root.findNode(nodeId) ?: return project
        val mutable = targetNode.modifiers.toMutableList()
        val safeIndex = index.coerceIn(0, mutable.size)
        mutable.add(safeIndex, mod)
        val newRoot = screen.root.setModifiers(nodeId, mutable)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Updates a modifier at a specific index. Undo restores previous modifier.
 */
data class UpdateModifierCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val index: Int,
    val newModifier: ModifierSpec
) : EditorCommand {
    private var oldModifier: ModifierSpec? = null

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val targetNode = screen.root.findNode(nodeId) ?: return project
        if (index !in targetNode.modifiers.indices) return project
        oldModifier = targetNode.modifiers[index]
        val newRoot = screen.root.updateModifier(nodeId, index, newModifier)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val old = oldModifier ?: return project
        val newRoot = screen.root.updateModifier(nodeId, index, old)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Reorders a modifier from one index to another. Undo reverses the reorder.
 */
data class ReorderModifierCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val fromIndex: Int,
    val toIndex: Int
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.reorderModifier(nodeId, fromIndex, toIndex)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.reorderModifier(nodeId, toIndex, fromIndex)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

/**
 * Duplicates a node with its subtree.
 * Assigns fresh IDs to every node in the copy, inserting it after the original.
 */
data class DuplicateNodeCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val duplicatedSubtree: DesignNode
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val parent = screen.root.findParent(nodeId) ?: return project

        // Find whether original is in a slot or in raw children
        val slotEntry = parent.slots.entries.firstOrNull { (_, slotNodes) -> slotNodes.any { it.id == nodeId } }
        val newRoot = if (slotEntry != null) {
            val originalIndex = slotEntry.value.indexOfFirst { it.id == nodeId }
            screen.root.insertNode(parent.id, duplicatedSubtree, originalIndex + 1, slotEntry.key)
        } else {
            val originalIndex = parent.children.indexOfFirst { it.id == nodeId }
            screen.root.insertNode(parent.id, duplicatedSubtree, originalIndex + 1, null)
        }
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.removeNode(duplicatedSubtree.id)
        return project.updateScreenRoot(screenId, newRoot)
    }
}

data class RemovedNodeRecord(
    val node: DesignNode,
    val parentId: NodeId?,
    val slotId: SlotId?,
    val index: Int
)

/**
 * Removes multiple nodes in one atomic command. Undo restores each node in its original position.
 */
data class RemoveNodesCommand(
    val screenId: ScreenId,
    val nodeIds: Set<NodeId>
) : EditorCommand {
    private var capturedRecords: List<RemovedNodeRecord>? = null

    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        if (capturedRecords == null) {
            val topLevelIds = nodeIds.filter { candidateId ->
                nodeIds.none { otherId -> otherId != candidateId && screen.root.isDescendantOf(candidateId, otherId) }
            }
            capturedRecords = topLevelIds.mapNotNull { id ->
                val node = screen.root.findNode(id) ?: return@mapNotNull null
                val parent = screen.root.findParent(id)
                val slotId = parent?.findSlotForNode(id)
                val index = if (parent == null) {
                    -1
                } else if (slotId != null) {
                    (parent.slots[slotId] ?: emptyList()).indexOfFirst { it.id == id }
                } else {
                    parent.children.indexOfFirst { it.id == id }
                }
                RemovedNodeRecord(node, parent?.id, slotId, index)
            }
        }

        var currentRoot = screen.root
        val records = capturedRecords ?: emptyList()
        for (record in records) {
            currentRoot = currentRoot.removeNode(record.node.id)
        }
        return project.updateScreenRoot(screenId, currentRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        var currentRoot = screen.root
        val records = capturedRecords ?: emptyList()
        for (record in records.sortedBy { it.index }) {
            val parentId = record.parentId ?: continue
            currentRoot = if (record.index >= 0) {
                currentRoot.insertNode(parentId, record.node, record.index, record.slotId)
            } else {
                currentRoot.addNode(parentId, record.node, record.slotId)
            }
        }
        return project.updateScreenRoot(screenId, currentRoot)
    }
}

data class NodeDuplicationEntry(
    val originalNodeId: NodeId,
    val duplicatedSubtree: DesignNode
)

/**
 * Duplicates multiple nodes in one atomic command.
 */
data class DuplicateNodesCommand(
    val screenId: ScreenId,
    val entries: List<NodeDuplicationEntry>
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        var currentRoot = screen.root
        for (entry in entries) {
            val parent = currentRoot.findParent(entry.originalNodeId) ?: continue
            val slotEntry = parent.slots.entries.firstOrNull { (_, slotNodes) -> slotNodes.any { it.id == entry.originalNodeId } }
            currentRoot = if (slotEntry != null) {
                val originalIndex = slotEntry.value.indexOfFirst { it.id == entry.originalNodeId }
                currentRoot.insertNode(parent.id, entry.duplicatedSubtree, originalIndex + 1, slotEntry.key)
            } else {
                val originalIndex = parent.children.indexOfFirst { it.id == entry.originalNodeId }
                currentRoot.insertNode(parent.id, entry.duplicatedSubtree, originalIndex + 1, null)
            }
        }
        return project.updateScreenRoot(screenId, currentRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        var currentRoot = screen.root
        for (entry in entries) {
            currentRoot = currentRoot.removeNode(entry.duplicatedSubtree.id)
        }
        return project.updateScreenRoot(screenId, currentRoot)
    }
}

/**
 * Renames a node's cosmetic label. Undo restores the previous label.
 */
data class RenameNodeCommand(
    val screenId: ScreenId,
    val nodeId: NodeId,
    val oldLabel: String?,
    val newLabel: String?
) : EditorCommand {
    override fun execute(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.renameNode(nodeId, newLabel)
        return project.updateScreenRoot(screenId, newRoot)
    }

    override fun undo(project: Project): Project {
        val screen = project.findScreen(screenId) ?: return project
        val newRoot = screen.root.renameNode(nodeId, oldLabel)
        return project.updateScreenRoot(screenId, newRoot)
    }
}