package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.State
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ScreenId
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

// 1. Ephemeral UI state (Selection, Locks, Viewport)
interface SelectionState {
    val selectedNodeId: State<NodeId?>
    val activeScreenId: State<ScreenId?>
    val lockedNodeIds: State<Set<NodeId>>
    fun selectNode(id: NodeId?)
    fun setActiveScreen(id: ScreenId)
    fun toggleLock(id: NodeId)
    fun isNodeLocked(id: NodeId): Boolean
}

// 2. Transaction & History Manager
interface HistoryState {
    val undoStack: StateFlow<List<EditorCommand>>
    val redoStack: StateFlow<List<EditorCommand>>
    fun execute(command: EditorCommand)
    fun undo()
    fun redo()
}

// 3. Persistence & AST Factory
interface ProjectSession {
    val project: StateFlow<Project?>
    val events: SharedFlow<EditorEvent>
    fun loadProject()
    fun saveProject()
    fun createNewProject()
    fun exportCode(customPackageName: String? = null)
}