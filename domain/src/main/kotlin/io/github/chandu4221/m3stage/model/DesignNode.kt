package io.github.chandu4221.m3stage.model

data class DesignNode(
    val id: NodeId,
    val type: ComponentType,
    val props: Map<PropId, PropVal> = emptyMap(),
    val modifiers: List<ModifierNode> = emptyList(),
    val children: List<DesignNode> = emptyList(),
    val isVisible: Boolean = true
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <V : PropVal> get(key: PropKey<V>): V? = props[key.id] as? V
}