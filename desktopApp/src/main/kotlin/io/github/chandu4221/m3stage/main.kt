package io.github.chandu4221.m3stage

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.materialkolor.rememberDynamicColorScheme
import io.github.chandu4221.m3stage.adapter.UuidIdGenerator
import io.github.chandu4221.m3stage.adapter.codegen.ComposeCodeGenerator
import io.github.chandu4221.m3stage.adapter.persistence.JsonProjectRepository
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.ui.EditorScreen
import java.io.File

fun main() = application {
    val store = remember {
        val projectFile = File(System.getProperty("user.home"), ".m3stage/project.json")
        projectFile.parentFile?.mkdirs()

        EditorStore(
            idGenerator = UuidIdGenerator(),
            repository = JsonProjectRepository(projectFile),
            codeGenerator = ComposeCodeGenerator()
        ).apply {
            loadProject()
        }
    }

    val project by store.project.collectAsState()
    val seedColor = Color(project?.seedColor ?: 0xFF6750A4L)
    val isDarkMode = project?.isDarkMode ?: false

    // Generates the complete 36-role Material 3 ColorScheme dynamically from the seed color!
    val dynamicColorScheme = rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = isDarkMode
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "m3stage - Material 3 Compose Builder"
    ) {
        MaterialTheme(colorScheme = dynamicColorScheme) {
            EditorScreen(store = store)
        }
    }
}