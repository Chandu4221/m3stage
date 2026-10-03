package io.github.chandu4221.m3stage.mutation

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId

/**
 * Updates the root node of a specific screen.
 * Returns a new immutable Project.
 */
fun Project.updateScreenRoot(screenId: ScreenId, newRoot: DesignNode): Project {
    val updatedScreens = screens.map { screen ->
        if (screen.id == screenId) {
            screen.copy(root = newRoot)
        } else {
            screen
        }
    }
    return copy(screens = updatedScreens)
}

/**
 * Adds a new screen to the project.
 */
fun Project.addScreen(screen: Screen): Project {
    return copy(screens = screens + screen)
}

/**
 * Removes a screen from the project.
 */
fun Project.removeScreen(screenId: ScreenId): Project {
    return copy(screens = screens.filter { it.id != screenId })
}