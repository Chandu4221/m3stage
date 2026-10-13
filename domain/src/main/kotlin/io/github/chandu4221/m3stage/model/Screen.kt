package io.github.chandu4221.m3stage.model

/**
 * Represents a single screen / destination within a [Project].
 */
data class Screen(
    val id: ScreenId,
    val name: String,
    val route: String,
    val root: DesignNode,
    val device: DevicePreset? = null,
    val canvasX: Float = 0f,
    val canvasY: Float = 0f,
) {
    init {
        require(name.isNotBlank()) { "Screen name cannot be blank" }
        require(isValidScreenName(name)) {
            "Screen name '$name' is invalid. Must be a valid identifier (alphanumeric and underscores, starting with letter)."
        }
        require(route.isNotBlank()) { "Screen route cannot be blank" }
        require(!route.contains(Regex("\\s"))) {
            "Screen route '$route' cannot contain whitespace."
        }
    }

    /**
     * Resolves the active device preset for this screen given the project default.
     */
    fun resolveDevice(projectDefault: DevicePreset): DevicePreset =
        device ?: projectDefault

    /**
     * Finds a node by [nodeId] anywhere in this screen's tree.
     */
    fun findNode(nodeId: NodeId): DesignNode? = root.findNode(nodeId)

    /**
     * Checks if this screen contains a node with [nodeId].
     */
    fun containsNode(nodeId: NodeId): Boolean = root.contains(nodeId)

    /**
     * Flattens and returns all nodes present on this screen.
     */
    val allNodes: List<DesignNode>
        get() {
            val result = mutableListOf<DesignNode>()
            fun collect(node: DesignNode) {
                result.add(node)
                for (child in node.allDirectChildren) {
                    collect(child)
                }
            }
            collect(root)
            return result
        }
}

private fun isValidScreenName(name: String): Boolean {
    val regex = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
    return regex.matches(name)
}