package io.github.chandu4221.m3stage.query

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentDefinition
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.DesignNode

/**
 * Finds the first slot in [this] definition that declares [childKind] in its allowed kinds.
 */
fun ComponentDefinition.findAcceptingSlot(childKind: ComponentKind): SlotId? =
    slots.firstOrNull { slot -> slot.canAccept(childKind) }?.id

/**
 * Finds the first direct child container (in slots or children) that can accept [childKind].
 */
fun findDefaultContainerChild(node: DesignNode, childKind: ComponentKind): DesignNode? {
    return node.allDirectChildren.firstOrNull { child ->
        val childDef = ComponentCatalog.find(child.kind)
        childDef != null && childDef.isContainer && (
            childDef.findAcceptingSlot(childKind) != null || childDef.canAcceptChild(childKind)
        )
    }
}

/**
 * Checks whether [childKind] can be placed inside [parent] node (either in one of its slots,
 * as a plain child, or inside a default container child).
 */
fun canPlace(parent: DesignNode, childKind: ComponentKind): Boolean {
    val parentDef = ComponentCatalog.find(parent.kind) ?: return false
    val childDef = ComponentCatalog.find(childKind)

    if (childDef?.slotOnly == true) {
        return parentDef.findAcceptingSlot(childKind) != null
    }

    if (parentDef.findAcceptingSlot(childKind) != null) return true
    if (parentDef.canAcceptChild(childKind)) return true

    return findDefaultContainerChild(parent, childKind) != null
}
