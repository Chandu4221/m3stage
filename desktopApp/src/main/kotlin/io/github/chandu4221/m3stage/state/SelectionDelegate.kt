package io.github.chandu4221.m3stage.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.ScreenId

class SelectionDelegate : SelectionState {
    private val _selectedNodeIds: MutableState<Set<NodeId>> = mutableStateOf(emptySet())
    override val selectedNodeIds: State<Set<NodeId>> = _selectedNodeIds

    override val selectedNodeId: State<NodeId?> = derivedStateOf { _selectedNodeIds.value.lastOrNull() }

    private val _activeScreenId: MutableState<ScreenId?> = mutableStateOf(null)
    override val activeScreenId: State<ScreenId?> = _activeScreenId

    private val _lockedNodeIds: MutableState<Set<NodeId>> = mutableStateOf(emptySet())
    override val lockedNodeIds: State<Set<NodeId>> = _lockedNodeIds

    private val _drillAnchorNodeId: MutableState<NodeId?> = mutableStateOf(null)
    override val drillAnchorNodeId: State<NodeId?> = _drillAnchorNodeId

    override fun selectNode(id: NodeId?) {
        _selectedNodeIds.value = if (id != null) setOf(id) else emptySet()
        if (id == null) {
            _drillAnchorNodeId.value = null
        }
    }

    override fun toggleNodeInSelection(id: NodeId) {
        val current = _selectedNodeIds.value
        _selectedNodeIds.value = if (id in current) current - id else current + id
        if (_selectedNodeIds.value.isEmpty()) {
            _drillAnchorNodeId.value = null
        }
    }

    override fun selectNodes(ids: Set<NodeId>) {
        _selectedNodeIds.value = ids
        if (ids.isEmpty()) {
            _drillAnchorNodeId.value = null
        }
    }

    override fun setActiveScreen(id: ScreenId?) {
        _activeScreenId.value = id
        _selectedNodeIds.value = emptySet()
        _drillAnchorNodeId.value = null
    }

    override fun toggleLock(id: NodeId) {
        _lockedNodeIds.value = if (id in _lockedNodeIds.value) _lockedNodeIds.value - id else _lockedNodeIds.value + id
    }

    override fun isNodeLocked(id: NodeId) = id in _lockedNodeIds.value

    override fun setDrillAnchor(id: NodeId?) {
        _drillAnchorNodeId.value = id
    }

    override fun popSelectionAnchor(
        treeInfo: Map<NodeId, io.github.chandu4221.m3stage.adapter.renderer.selection.NodeTreeInfo>?,
        screenRootId: NodeId?
    ) {
        val currentPrimary = selectedNodeId.value ?: return
        val rootId = screenRootId ?: return
        val info = treeInfo ?: return

        val (newSelected, newAnchor) = io.github.chandu4221.m3stage.adapter.renderer.selection.SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = _drillAnchorNodeId.value,
            selectedNodeId = currentPrimary,
            screenRootId = rootId,
            treeInfo = info
        )
        _selectedNodeIds.value = if (newSelected != null) setOf(newSelected) else emptySet()
        _drillAnchorNodeId.value = newAnchor
    }
}