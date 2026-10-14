package io.github.chandu4221.m3stage.adapter.persistence.dto

import kotlinx.serialization.Serializable

/**
 * DesignNode serialization DTO supporting explicit slots and children hierarchy.
 */
@Serializable
data class DesignNodeDto(
    val id: String,
    val type: String,
    val props: Map<String, PropValDto> = emptyMap(),
    val modifiers: List<ModifierNodeDto> = emptyList(),
    val children: List<DesignNodeDto> = emptyList(),
    val slots: Map<String, List<DesignNodeDto>> = emptyMap(),
    val isVisible: Boolean = true,
    val isPrefilled: Boolean = false
)
