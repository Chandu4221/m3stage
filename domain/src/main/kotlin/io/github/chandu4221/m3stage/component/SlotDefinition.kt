package io.github.chandu4221.m3stage.component

@JvmInline
value class SlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "SlotId cannot be blank" }
    }
}

data class SlotDefinition(
    val id: SlotId,
    val displayName: String,
    val isRequired: Boolean = false,
    val maxChildren: Int? = null,
    val allowedKinds: Set<ComponentKind>? = null
)