package io.github.chandu4221.m3stage.model

data class Screen(
    val id: ScreenId,
    val name: String,
    val route: String,
    val root: DesignNode,
    val device: DevicePreset? = null,
) {
    init {
        require(name.isNotBlank()) { "Screen name cannot be blank" }
        require(route.isNotBlank()) { "Screen route cannot be blank" }
    }

    /**
     * Resolves the active device preset for this screen given the project default.
     */
    fun resolveDevice(projectDefault: DevicePreset): DevicePreset =
        device ?: projectDefault
}