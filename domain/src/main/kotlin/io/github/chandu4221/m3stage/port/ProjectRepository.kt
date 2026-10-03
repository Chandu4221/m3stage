package io.github.chandu4221.m3stage.port

import io.github.chandu4221.m3stage.model.Project

/**
 * Port for saving and loading the project document.
 * Marked as suspend because file I/O or network calls should not block the UI thread.
 */
interface ProjectRepository {
    suspend fun save(project: Project)
    suspend fun load(): Project?
}