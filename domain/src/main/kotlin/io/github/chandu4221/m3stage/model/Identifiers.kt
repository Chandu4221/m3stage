package io.github.chandu4221.m3stage.model

@JvmInline
value class ProjectId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}

@JvmInline
value class ScreenId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}


@JvmInline
value class NodeId(val value: String) {
    init {
        require(value.isNotBlank()) { "NodeId cannot be blank" }
    }
}

@JvmInline
value class SlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "SlotId cannot be blank" }
    }
}

@JvmInline
value class ComponentType(val value: String) {
    init {
        require(value.isNotBlank()) { "ComponentType cannot be blank" }
    }
}