package io.github.chandu4221.m3stage.adapter.persistence

import io.github.chandu4221.m3stage.adapter.persistence.dto.ProjectDto
import io.github.chandu4221.m3stage.adapter.persistence.dto.toDomain
import io.github.chandu4221.m3stage.adapter.persistence.dto.toDto
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.port.ProjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Adapter implementation of ProjectRepository using local file system and JSON.
 */
class JsonProjectRepository(
    private val file: File,
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
) : ProjectRepository {

    override suspend fun save(project: Project) {
        withContext(Dispatchers.IO) {
            val dto = project.toDto()
            val jsonString = json.encodeToString(dto)
            file.writeText(jsonString)
        }
    }

    override suspend fun load(): Project? {
        return withContext(Dispatchers.IO) {
            if (!file.exists()) return@withContext null

            try {
                val jsonString = file.readText()
                val dto = json.decodeFromString<ProjectDto>(jsonString)
                dto.toDomain()
            } catch (e: Exception) {
                // In a real app, log this error. For now, return null on corrupt file.
                null
            }
        }
    }
}