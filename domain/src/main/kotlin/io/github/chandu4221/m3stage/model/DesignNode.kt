package io.github.chandu4221.m3stage.model

data class DesignNode(
    val id: NodeId,
    val type: ComponentType,
    val props: Map<String, String> = emptyMap(),
    val children: List<DesignNode> = emptyList()
)
