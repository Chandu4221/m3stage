package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.ScreenId

class SelectionDelegate : SelectionState {
    private val _selectedNodeId: MutableState<NodeId?> = mutableStateOf(null)
    override val selectedNodeId: State<NodeId?> = _selectedNodeId

    private val _activeScreenId: MutableState<ScreenId?> = mutableStateOf(null)
    override val activeScreenId: State<ScreenId?> = _activeScreenId

    private val _lockedNodeIds: MutableState<Set<NodeId>> = mutableStateOf(emptySet())
    override val lockedNodeIds: State<Set<NodeId>> = _lockedNodeIds

    override fun selectNode(id: NodeId?) {
        _selectedNodeId.value = id
    }

    override fun setActiveScreen(id: ScreenId) {
        _activeScreenId.value = id; _selectedNodeId.value = null
    }

    override fun toggleLock(id: NodeId) {
        _lockedNodeIds.value = if (id in _lockedNodeIds.value) _lockedNodeIds.value - id else _lockedNodeIds.value + id
    }

    override fun isNodeLocked(id: NodeId) = id in _lockedNodeIds.value
}