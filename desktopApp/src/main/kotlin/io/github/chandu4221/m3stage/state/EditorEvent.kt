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
}