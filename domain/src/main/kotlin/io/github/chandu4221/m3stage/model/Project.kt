package io.github.chandu4221.m3stage.model

data class Project(
    val id: ProjectId,
    val name: String,
    val basePackage: String,
    val defaultDevice: DevicePreset = DevicePreset.Default,
    val screens: List<Screen> = emptyList()
) {
    init {
        require(name.isNotBlank()) { "Project name cannot be blank" }
        require(isValidPackageName(basePackage)) {
            "Base package is invalid"
        }

        val uniqueScreenIds = screens.map { it.id }.distinct()
        require(uniqueScreenIds.size == screens.size) {
            "Screen ids must be unique"
        }
    }
}

private fun isValidPackageName(value: String): Boolean {
    if (value.isBlank()) return false

    val packageRegex = Regex(
        "^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$"
    )

    return packageRegex.matches(value)
}