package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryDelegate(private val context: EditorContext) : HistoryState {
    private val _undoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    override val undoStack: StateFlow<List<EditorCommand>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    override val redoStack: StateFlow<List<EditorCommand>> = _redoStack.asStateFlow()

    override fun execute(command: EditorCommand) {
        val current = context.projectFlow.value ?: return
        val newProject = try {
            command.execute(current)
        } catch (e: Exception) {
            context.eventFlow.tryEmit(EditorEvent.ShowError(e.message ?: "Invalid operation"))
            return
        }

        val errors = ProjectValidation.validate(newProject)
        if (errors.isNotEmpty()) {
            context.eventFlow.tryEmit(EditorEvent.ShowError(errors.first()))
            return
        }

        context.projectFlow.value = newProject
        _undoStack.value = _undoStack.value + command
        _redoStack.value = emptyList()
    }

    override fun undo() {
        val command = _undoStack.value.lastOrNull() ?: return
        val current = context.projectFlow.value ?: return
        context.projectFlow.value = command.undo(current)
        _undoStack.value = _undoStack.value.dropLast(1)
        _redoStack.value = _redoStack.value + command
    }

    override fun redo() {
        val command = _redoStack.value.lastOrNull() ?: return
        val current = context.projectFlow.value ?: return
        context.projectFlow.value = command.execute(current)
        _redoStack.value = _redoStack.value.dropLast(1)
        _undoStack.value = _undoStack.value + command
    }
}