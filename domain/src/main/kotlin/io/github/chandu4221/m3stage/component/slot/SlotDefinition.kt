package io.github.chandu4221.m3stage.component.slot

import io.github.chandu4221.m3stage.component.ComponentKind

/**
 * Specification and atomic rules for a named component slot.
 *
 * @property id The unique identifier of this slot within the component.
 * @property displayName Human-readable label for the UI (e.g. "Top App Bar", "Actions").
 * @property isRequired Whether this slot must contain at least one node to be valid.
 * @property maxChildren Maximum number of children allowed in this slot (null = unbounded).
 * @property allowedKinds Specific [ComponentKind]s allowed in this slot (null = any kind allowed).
 */
data class SlotDefinition(
    val id: SlotId,
    val displayName: String,
    val isRequired: Boolean = false,
    val maxChildren: Int? = null,
    val allowedKinds: Set<ComponentKind>? = null
) {
    /**
     * Checks if a given [ComponentKind] is permitted inside this slot.
     */
    fun canAccept(kind: ComponentKind): Boolean =
        allowedKinds == null || kind in allowedKinds

    /**
     * Checks if this slot has reached its maximum child capacity.
     */
    fun isFull(currentCount: Int): Boolean =
        maxChildren != null && currentCount >= maxChildren
}