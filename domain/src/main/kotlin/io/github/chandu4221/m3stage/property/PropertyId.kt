package io.github.chandu4221.m3stage.property

@JvmInline
value class PropertyId(val value: String) {
    init {
        require(value.isNotBlank()) { "PropertyId cannot be blank" }
    }
}