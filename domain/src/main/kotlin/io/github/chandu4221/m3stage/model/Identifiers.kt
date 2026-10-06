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