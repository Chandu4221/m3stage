package io.github.chandu4221.m3stage.ui.page

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.query.flatten
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportController
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.state.ShellPanelsController

/**
 * Screen-local callbacks for [EditorKeyboardShortcuts].
 */
data class EditorShortcutActions(
    val isTextInputFocused: () -> Boolean = { false },
    val isPaletteDragging: () -> Boolean = { false },
    val onCancelPaletteDrag: () -> Unit = {}
)

/**
 * Centralized key-event handler for the editor shell.
 *
 * Supports plain JUnit testing without Compose UI rules.
 */
fun handleEditorKeyEvent(
    keyEvent: KeyEvent,
    store: EditorStore,
    panels: ShellPanelsController,
    viewportController: CanvasViewportController,
    actions: EditorShortcutActions = EditorShortcutActions()
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    // 0. Panel Shortcuts (Ctrl+Shift+1/2/3)
    if (keyEvent.isCtrlPressed && keyEvent.isShiftPressed) {
        when (keyEvent.key) {
            Key.One, Key.NumPad1 -> {
                panels.toggleLeft()
                return true
            }
            Key.Two, Key.NumPad2 -> {
                panels.toggleRight()
                return true
            }
            Key.Three, Key.NumPad3 -> {
                panels.toggleBottom()
                return true
            }
        }
    }

    // 1. Undo / Redo (Ctrl+Z / Ctrl+Shift+Z / Ctrl+Y)
    if (keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.Z) {
        if (store.undoStack.value.isNotEmpty() || store.redoStack.value.isNotEmpty()) {
            store.redo()
            return true
        }
    } else if (keyEvent.isCtrlPressed && keyEvent.key == Key.Z) {
        if (store.undoStack.value.isNotEmpty()) {
            store.undo()
            return true
        }
    } else if (keyEvent.isCtrlPressed && keyEvent.key == Key.Y) {
        if (store.redoStack.value.isNotEmpty()) {
            store.redo()
            return true
        }
    }

    // 2. Duplicate: Ctrl+D
    if (keyEvent.isCtrlPressed && keyEvent.key == Key.D) {
        val currentSelected = store.selectedNodeId.value
        if (currentSelected != null) {
            store.duplicateNode(currentSelected)
            return true
        }
    }

    // 2.1. Clipboard operations: Ctrl+C, Ctrl+X, Ctrl+V (suppressed when typing in text fields)
    if (keyEvent.isCtrlPressed && !keyEvent.isShiftPressed && !actions.isTextInputFocused()) {
        when (keyEvent.key) {
            Key.C -> {
                val currentSelected = store.selectedNodeId.value
                if (currentSelected != null) {
                    store.copyNode(currentSelected)
                    return true
                }
            }
            Key.X -> {
                val currentSelected = store.selectedNodeId.value
                if (currentSelected != null) {
                    store.cutNode(currentSelected)
                    return true
                }
            }
            Key.V -> {
                if (store.clipboardDelegate.clip.value != null) {
                    store.pasteNode()
                    return true
                }
            }
        }
    }

    // 2.5. Save: Ctrl+S
    if (keyEvent.isCtrlPressed && keyEvent.key == Key.S) {
        store.saveProject()
        return true
    }

    // 2.6. Zoom: Ctrl+(+/-/0)
    if (keyEvent.isCtrlPressed) {
        if (keyEvent.key == Key.Equals || keyEvent.key == Key.Plus || keyEvent.key == Key.NumPadAdd) {
            viewportController.zoomIn()
            return true
        } else if (keyEvent.key == Key.Minus || keyEvent.key == Key.NumPadSubtract) {
            viewportController.zoomOut()
            return true
        } else if (keyEvent.key == Key.Zero || keyEvent.key == Key.NumPad0) {
            viewportController.resetZoom()
            return true
        }
    }

    // 3. Escape: 3-level cascade (cancel palette drag -> pop selection anchor / deselect node -> deselect screen)
    if (keyEvent.key == Key.Escape) {
        if (actions.isPaletteDragging()) {
            actions.onCancelPaletteDrag()
            return true
        }
        val selectedNodeId = store.selectedNodeId.value
        if (selectedNodeId != null) {
            val activeScreenId = store.activeScreenId.value
            val activeScreen = store.project.value?.screens?.firstOrNull { it.id == activeScreenId }
            val root = activeScreen?.root
            if (root != null) {
                val treeInfo = root.toTreeInfo()
                store.popSelectionAnchor(treeInfo, root.id)
            } else {
                store.selectNode(null)
            }
            return true
        }
        if (store.activeScreenId.value != null) {
            store.setActiveScreen(null)
            return true
        }
        return false
    }

    // 4. Delete / Backspace: remove node (suppressed when typing in text fields)
    if (keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace) {
        if (actions.isTextInputFocused()) return false
        val currentSelected = store.selectedNodeId.value
        if (currentSelected != null) {
            store.removeNode(currentSelected)
            return true
        }
    }

    // 5. Arrow Up / Down in Layers tree (suppressed when typing in text fields)
    if (keyEvent.key == Key.DirectionUp || keyEvent.key == Key.DirectionDown) {
        if (actions.isTextInputFocused()) return false
        val activeScreenId = store.activeScreenId.value
        val screen = store.project.value?.screens?.firstOrNull { it.id == activeScreenId }
        if (screen != null) {
            val allNodes = screen.root.flatten().filter { it.isVisible }
            if (allNodes.isNotEmpty()) {
                val selectedNodeId = store.selectedNodeId.value
                val currentIndex = allNodes.indexOfFirst { it.id == selectedNodeId }
                val newIndex = if (keyEvent.key == Key.DirectionUp) {
                    if (currentIndex <= 0) allNodes.lastIndex else currentIndex - 1
                } else {
                    if (currentIndex == -1 || currentIndex >= allNodes.lastIndex) 0 else currentIndex + 1
                }
                store.selectNode(allNodes[newIndex].id)
                return true
            }
        }
    }

    // 6. Tool Shortcuts (V: Select, H: Pan; suppressed when typing in text fields or modifiers active)
    if (!keyEvent.isCtrlPressed && !keyEvent.isAltPressed && !keyEvent.isMetaPressed) {
        if (actions.isTextInputFocused()) return false
        if (keyEvent.key == Key.V) {
            viewportController.setActiveTool(CanvasPointerTool.Select)
            return true
        } else if (keyEvent.key == Key.H) {
            viewportController.setActiveTool(CanvasPointerTool.Pan)
            return true
        }
    }

    return false
}

/**
 * Modifier extension wiring editor keyboard shortcuts to [store], [panels], and [viewportController].
 */
fun Modifier.editorKeyboardShortcuts(
    store: EditorStore,
    panels: ShellPanelsController,
    viewportController: CanvasViewportController,
    actions: EditorShortcutActions = EditorShortcutActions()
): Modifier = this.onPreviewKeyEvent { keyEvent ->
    handleEditorKeyEvent(keyEvent, store, panels, viewportController, actions)
}
