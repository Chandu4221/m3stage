package io.github.chandu4221.m3stage.query

import io.github.chandu4221.m3stage.model.*

/**
 * Finds a screen by its ID.
 */
fun Project.findScreen(id: ScreenId): Screen? {
    return screens.find { it.id == id }
}

/**
 * Finds a specific node within a specific screen.
 */
fun Project.findNode(screenId: ScreenId, nodeId: NodeId): DesignNode? {
    val screen = findScreen(screenId) ?: return null
    return screen.root.findNode(nodeId)
}

/**
 * Finds which screen contains a specific node ID.
 * Useful for global selection sync across the editor.
 */
fun Project.findScreenContaining(nodeId: NodeId): Screen? {
    return screens.find { screen ->
        screen.root.id == nodeId || screen.root.containsNode(nodeId)
    }
}

/**
 * Checks if a node exists anywhere in the project.
 */
fun Project.containsNode(nodeId: NodeId): Boolean {
    return screens.any { screen ->
        screen.root.id == nodeId || screen.root.containsNode(nodeId)
    }
}