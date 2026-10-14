package io.github.chandu4221.m3stage.model

/**
 * Root document entity representing an entire M3Stage project.
 */
data class Project(
    val id: ProjectId,
    val name: String,
    val basePackage: String,
    val defaultDevice: DevicePreset = DevicePreset.Default,
    val seedColor: Long = 0xFF6750A4L,
    val isDarkMode: Boolean = false,
    val screens: List<Screen> = emptyList()
) {
    init {
        require(name.isNotBlank()) { "Project name cannot be blank" }
        require(isValidPackageName(basePackage)) {
            "Base package '$basePackage' is invalid. Must follow standard Java/Kotlin package naming rules."
        }

        // 1. Screen ID Uniqueness
        val uniqueScreenIds = screens.map { it.id }.distinct()
        require(uniqueScreenIds.size == screens.size) {
            "Screen IDs must be unique across the project."
        }

        // 2. Screen Name Uniqueness (prevents codegen file collision)
        val uniqueScreenNames = screens.map { it.name.lowercase() }.distinct()
        require(uniqueScreenNames.size == screens.size) {
            "Screen names must be unique (case-insensitive) across the project."
        }

        // 3. Navigation Route Uniqueness (prevents runtime navigation collision)
        val uniqueRoutes = screens.map { it.route }.distinct()
        require(uniqueRoutes.size == screens.size) {
            "Screen routes must be unique across the project."
        }
    }

    /**
     * Looks up a screen by its [ScreenId].
     */
    fun findScreen(screenId: ScreenId): Screen? =
        screens.firstOrNull { it.id == screenId }

    /**
     * Looks up a screen by its navigation [route].
     */
    fun findScreenByRoute(route: String): Screen? =
        screens.firstOrNull { it.route == route }

    /**
     * Searches across all screens for a node with [nodeId],
     * returning the enclosing [Screen] alongside the found [DesignNode].
     */
    fun findNode(nodeId: NodeId): Pair<Screen, DesignNode>? {
        for (screen in screens) {
            val node = screen.findNode(nodeId)
            if (node != null) return screen to node
        }
        return null
    }
    companion object {
        private val PACKAGE_REGEX = Regex("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$")

        /**
         * Validates standard Java/Kotlin package naming convention:
         * - Non-blank
         * - Segments separated by dots
         * - Each segment starts with letter/underscore followed by letters/digits/underscores
         */
        fun isValidPackageName(value: String): Boolean {
            val trimmed = value.trim()
            if (trimmed.isBlank()) return false
            return PACKAGE_REGEX.matches(trimmed)
        }

        /**
         * Validates project name: must not be blank.
         */
        fun isValidProjectName(value: String): Boolean {
            return value.isNotBlank()
        }
    }
}