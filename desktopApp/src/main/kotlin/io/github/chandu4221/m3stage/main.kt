package io.github.chandu4221.m3stage

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.chandu4221.m3stage.adapter.UuidIdGenerator
import io.github.chandu4221.m3stage.adapter.persistence.JsonProjectRepository
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.EditorScreen
import java.io.File

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "M3 Stage Editor"
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                val store = remember { createEditorStore() }

                // Load existing project on startup
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    store.loadProject()
                }

                EditorScreen(store = store)
            }
        }
    }
}

private fun createEditorStore(): EditorStore {
    val idGenerator = UuidIdGenerator()
    val projectFile = File(System.getProperty("user.home"), "m3stage-project.json")
    val repository = JsonProjectRepository(projectFile)

    return EditorStore(
        idGenerator = idGenerator,
        repository = repository
    )
}