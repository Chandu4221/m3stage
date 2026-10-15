package io.github.chandu4221.m3stage.ui.page

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.query.findNearestAcceptingAncestor
import io.github.chandu4221.m3stage.query.findNode

/**
 * Computes disallowed component kinds for palette tiles based on active screen and selection.
 */
fun computeDisallowedKinds(
    project: Project?,
    activeScreenId: ScreenId?,
    selectedNodeId: NodeId?
): Set<ComponentKind> {
    val screen = project?.screens?.firstOrNull { it.id == activeScreenId }
    val root = screen?.root ?: return emptySet()
    val targetNode = (selectedNodeId?.let { root.findNode(it) }) ?: root
    return ComponentKind.entries.filterNot { kind ->
        targetNode.findNearestAcceptingAncestor(root, kind) != null
    }.toSet()
}
