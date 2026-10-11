package io.github.chandu4221.m3stage.adapter.persistence.dto

import kotlinx.serialization.Serializable

/**
 * Project and Screen serialization DTOs.
 */

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val basePackage: String,
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
    val root: DesignNodeDto
)
