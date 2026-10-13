package io.github.chandu4221.m3stage.adapter.persistence.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Project and Screen serialization DTOs.
 * Migration rule: CURRENT_SCHEMA_VERSION is incremented on breaking schema changes.
 * If schemaVersion > CURRENT_SCHEMA_VERSION on load, load fails with an incompatibility message.
 */
const val CURRENT_SCHEMA_VERSION = 1

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val basePackage: String,
    @SerialName("schemaVersion") val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val defaultDevice: String = "pixel_8",
    val seedColor: Long = 0xFF6750A4L,
    val isDarkMode: Boolean = false,
    val screens: List<ScreenDto>
)

@Serializable
data class ScreenDto(
    val id: String,
    val name: String,
    val route: String,
    val device: String? = null,
    val canvasX: Float = 0f,
    val canvasY: Float = 0f,
    val root: DesignNodeDto
)
