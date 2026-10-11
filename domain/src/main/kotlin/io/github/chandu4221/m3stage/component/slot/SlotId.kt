package io.github.chandu4221.m3stage.component.slot

/**
 * Strongly-typed identifier for a named child slot (e.g. "topBar", "title", "content").
 */
@JvmInline
value class SlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "SlotId cannot be blank" }
    }

    override fun toString(): String = value
}