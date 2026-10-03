package io.github.chandu4221.m3stage.adapter.persistence.dto

import io.github.chandu4221.m3stage.model.*
import kotlinx.serialization.Serializable

/**
 * DTOs are used strictly for serialization.
 * They prevent tying the domain model to @Serializable annotations.
 */

@Serializable
data class ProjectDto(
    val id: String,
    val name: String,
    val basePackage: String,
    val screens: List<ScreenDto>
)

@Serializable
data class ScreenDto(
    val id: String,
    val name: String,
    val route: String,
    val root: DesignNodeDto
)

@Serializable
data class DesignNodeDto(
    val id: String,
    val type: String,
    val props: Map<String, String> = emptyMap(),
    val children: List<DesignNodeDto> = emptyList()
)

// --- Mappers: Convert between Domain and DTO ---

fun Project.toDto(): ProjectDto = ProjectDto(
    id = this.id.value,
    name = this.name,
    basePackage = this.basePackage,
    screens = this.screens.map { it.toDto() }
)

fun ProjectDto.toDomain(): Project = Project(
    id = ProjectId(this.id),
    name = this.name,
    basePackage = this.basePackage,
    screens = this.screens.map { it.toDomain() }
)

fun Screen.toDto(): ScreenDto = ScreenDto(
    id = this.id.value,
    name = this.name,
    route = this.route,
    root = this.root.toDto()
)

fun ScreenDto.toDomain(): Screen = Screen(
    id = ScreenId(this.id),
    name = this.name,
    route = this.route,
    root = this.root.toDomain()
)

fun DesignNode.toDto(): DesignNodeDto = DesignNodeDto(
    id = this.id.value,
    type = this.type.value,
    props = this.props,
    children = this.children.map { it.toDto() }
)

fun DesignNodeDto.toDomain(): DesignNode = DesignNode(
    id = NodeId(this.id),
    type = ComponentType(this.type),
    props = this.props,
    children = this.children.map { it.toDomain() }
)