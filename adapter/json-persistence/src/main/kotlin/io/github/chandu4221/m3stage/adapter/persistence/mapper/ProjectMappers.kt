package io.github.chandu4221.m3stage.adapter.persistence.mapper

import io.github.chandu4221.m3stage.adapter.persistence.dto.ProjectDto
import io.github.chandu4221.m3stage.adapter.persistence.dto.ScreenDto
import io.github.chandu4221.m3stage.model.*

fun Project.toDto(): ProjectDto = ProjectDto(
    id = this.id.value,
    name = this.name,
    basePackage = this.basePackage,
    defaultDevice = this.defaultDevice.id,
    screens = this.screens.map { it.toDto() },
    seedColor = this.seedColor,
    isDarkMode = this.isDarkMode,
)

fun Screen.toDto(): ScreenDto = ScreenDto(
    id = this.id.value,
    name = this.name,
    route = this.route,
    device = this.device?.id,
    orientation = this.orientation.name,
    canvasX = this.canvasX,
    canvasY = this.canvasY,
    root = this.root.toDto()
)

fun ProjectDto.toDomain(): Project = Project(
    id = ProjectId(this.id),
    name = this.name,
    basePackage = this.basePackage,
    defaultDevice = DevicePreset.fromId(this.defaultDevice),
    screens = this.screens.map { it.toDomain() },
    seedColor = this.seedColor,
    isDarkMode = this.isDarkMode,
)

fun ScreenDto.toDomain(): Screen = Screen(
    id = ScreenId(this.id),
    name = this.name,
    route = this.route,
    device = this.device?.let { DevicePreset.fromId(it) },
    orientation = try {
        DeviceOrientation.valueOf(this.orientation)
    } catch (_: Exception) {
        DeviceOrientation.Portrait
    },
    canvasX = this.canvasX,
    canvasY = this.canvasY,
    root = this.root.toDomain()
)
