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
    val selectedNodeIds: State<Set<NodeId>>
    val activeScreenId: State<ScreenId?>
    val lockedNodeIds: State<Set<NodeId>>
    val drillAnchorNodeId: State<NodeId?>
    fun selectNode(id: NodeId?)
    fun toggleNodeInSelection(id: NodeId)
    fun selectNodes(ids: Set<NodeId>)
    fun setActiveScreen(id: ScreenId?)
    fun toggleLock(id: NodeId)
    fun isNodeLocked(id: NodeId): Boolean
    fun popSelectionAnchor(treeInfo: Map<NodeId, io.github.chandu4221.m3stage.adapter.renderer.selection.NodeTreeInfo>? = null, screenRootId: NodeId? = null)
    fun setDrillAnchor(id: NodeId?)
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
    val isDirty: StateFlow<Boolean>
    fun loadProject()
    fun saveProject()
    fun createNewProject()
    fun exportCode(customPackageName: String? = null)
}