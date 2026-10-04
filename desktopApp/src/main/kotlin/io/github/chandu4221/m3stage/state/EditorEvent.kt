package io.github.chandu4221.m3stage.state


/**
 * One-shot events that the UI should react to (toasts, errors, etc).
 * These are NOT state — they are transient notifications.
 */
sealed interface EditorEvent {
    data class ShowToast(val message: String) : EditorEvent
    data class ShowError(val message: String) : EditorEvent
    data object SaveSuccess : EditorEvent
    data object SaveFailed : EditorEvent

    data object LoadSuccess : EditorEvent
    data object LoadFailed : EditorEvent

    // Add these to the sealed interface:
    data class ExportSuccess(val path: String) : EditorEvent
    data class ExportFailed(val message: String) : EditorEvent
}