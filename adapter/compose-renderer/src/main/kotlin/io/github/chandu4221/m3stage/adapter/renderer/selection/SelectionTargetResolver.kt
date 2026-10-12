package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import io.github.chandu4221.m3stage.model.NodeId

/**
 * Resolves the single-click and double-click selection target according to the Figma drill-down state machine.
 */
object SelectionTargetResolver {

    /**
     * Determines whether [candidate] is a descendant of [ancestor] (or equal).
     */
    fun isDescendantOf(
        candidate: NodeId,
        ancestor: NodeId,
        treeInfo: Map<NodeId, NodeTreeInfo>
    ): Boolean {
        var curr: NodeId? = candidate
        while (curr != null) {
            if (curr == ancestor) return true
            curr = treeInfo[curr]?.parent
        }
        return false
    }

    /**
     * Resolves single-click target:
     * - If point inside anchor subtree:
     *     Select outermost container descendant of anchor containing the point.
     *     If no container descendant, select deepest direct-child leaf of anchor containing point.
     * - If point outside anchor subtree but inside screen root:
     *     Reset anchor to screen root, then apply above rule.
     * - If outside screen root:
     *     null (deselect).
     */
    fun resolveSingleClickTarget(
        point: Offset,
        anchor: NodeId?,
        screenRootId: NodeId,
        treeInfo: Map<NodeId, NodeTreeInfo>,
        bounds: Map<NodeId, Rect>
    ): Pair<NodeId?, NodeId?> {
        if (!point.isSpecified) return null to screenRootId

        // Check if point hits the screen root at all
        val rootRect = bounds[screenRootId]
        if (rootRect == null || !rootRect.contains(point)) {
            return null to screenRootId // Empty artboard click -> deselect, reset anchor
        }

        // Active anchor must be a valid node containing the point, otherwise fall back to screenRootId
        val effectiveAnchor = if (anchor != null && isDescendantOf(anchor, screenRootId, treeInfo) && bounds[anchor]?.contains(point) == true) {
            anchor
        } else {
            screenRootId
        }

        // Find all descendants of effectiveAnchor containing point
        val containingDescendants = bounds.entries
            .filter { (id, rect) ->
                id != effectiveAnchor && isDescendantOf(id, effectiveAnchor, treeInfo) && rect.contains(point)
            }
            .mapNotNull { (id, _) -> treeInfo[id] }
            .sortedBy { it.depth } // shallowest (outermost) first

        // Outermost container descendant of effectiveAnchor
        val outermostContainer = containingDescendants.firstOrNull { it.isContainer }
        if (outermostContainer != null) {
            return outermostContainer.id to effectiveAnchor
        }

        // If no container descendant, look for direct child leaf of effectiveAnchor
        val directChildLeaf = containingDescendants
            .filter { it.parent == effectiveAnchor && !it.isContainer }
            .maxByOrNull { it.depth }

        if (directChildLeaf != null) {
            return directChildLeaf.id to effectiveAnchor
        }

        // If no child matched, clicking inside effectiveAnchor selects effectiveAnchor itself
        return effectiveAnchor to effectiveAnchor
    }

    /**
     * Resolves double-click target:
     * - anchor becomes what single-click would select.
     * - then selects the deepest node under cursor within the NEW anchor subtree.
     * - If single-click selects a childless leaf, anchor is unchanged and leaf is selected.
     */
    fun resolveDoubleClickTarget(
        point: Offset,
        anchor: NodeId?,
        screenRootId: NodeId,
        treeInfo: Map<NodeId, NodeTreeInfo>,
        bounds: Map<NodeId, Rect>
    ): Pair<NodeId?, NodeId?> {
        val (singleTarget, currentAnchor) = resolveSingleClickTarget(point, anchor, screenRootId, treeInfo, bounds)
        if (singleTarget == null) return null to screenRootId

        val targetInfo = treeInfo[singleTarget]
        if (targetInfo == null || !targetInfo.isContainer || targetInfo.children.isEmpty()) {
            // Childless leaf or non-container: select leaf, keep anchor
            return singleTarget to currentAnchor
        }

        val newAnchor = singleTarget
        // Select deepest node under cursor inside newAnchor subtree
        val deepestInsideNewAnchor = bounds.entries
            .filter { (id, rect) -> isDescendantOf(id, newAnchor, treeInfo) && rect.contains(point) }
            .mapNotNull { (id, _) -> treeInfo[id] }
            .sortedWith(
                compareByDescending<NodeTreeInfo> { it.depth }
                    .thenBy { bounds[it.id]?.let { r -> r.width * r.height } ?: Float.MAX_VALUE }
            )
            .firstOrNull()?.id ?: newAnchor

        return deepestInsideNewAnchor to newAnchor
    }

    /**
     * Resolves Escape key action:
     * - If anchor != screenRootId: anchor = parent(anchor), selection unchanged.
     * - If anchor == screenRootId: full deselect (selectedNodeId = null).
     */
    fun resolveEscapeAction(
        currentAnchor: NodeId?,
        selectedNodeId: NodeId?,
        screenRootId: NodeId,
        treeInfo: Map<NodeId, NodeTreeInfo>
    ): Pair<NodeId?, NodeId?> {
        if (selectedNodeId == null) {
            return null to screenRootId
        }
        val effectiveAnchor = currentAnchor ?: screenRootId
        if (effectiveAnchor == screenRootId) {
            return null to screenRootId // Deselect
        }

        val parentAnchor = treeInfo[effectiveAnchor]?.parent ?: screenRootId
        return selectedNodeId to parentAnchor
    }
}
