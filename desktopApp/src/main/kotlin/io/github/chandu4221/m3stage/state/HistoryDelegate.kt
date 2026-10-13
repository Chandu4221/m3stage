package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryDelegate(
    private val context: EditorContext,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : HistoryState, AutoCloseable {

    private val _undoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    override val undoStack: StateFlow<List<EditorCommand>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    override val redoStack: StateFlow<List<EditorCommand>> = _redoStack.asStateFlow()

    private var lastCoalescibleKey: Pair<Triple<io.github.chandu4221.m3stage.model.ScreenId, io.github.chandu4221.m3stage.model.NodeId, Int>, Long>? = null

    @Synchronized
    override fun execute(command: EditorCommand) {
        val base = context.projectFlow.value
        if (base == null) {
            scope.launch {
                context.eventFlow.emit(EditorEvent.ShowError("No active project to execute command on"))
            }
            return
        }

        val newProject = try {
            command.execute(base)
        } catch (e: Exception) {
            scope.launch {
                context.eventFlow.emit(EditorEvent.ShowError(e.message ?: "Invalid operation"))
            }
            return
        }

        val errors = ProjectValidation.validate(newProject)
        if (errors.isNotEmpty()) {
            scope.launch {
                context.eventFlow.emit(EditorEvent.ShowError(errors.first()))
            }
            return
        }

        val now = System.currentTimeMillis()
        val shouldCoalesce = if (command is UpdateModifierCommand) {
            val key = Triple(command.screenId, command.nodeId, command.index)
            val last = lastCoalescibleKey
            if (last != null && last.first == key && (now - last.second) <= 300) {
                lastCoalescibleKey = Pair(key, now)
                true
            } else {
                lastCoalescibleKey = Pair(key, now)
                false
            }
        } else {
            lastCoalescibleKey = null
            false
        }

        context.projectFlow.value = newProject
        if (!shouldCoalesce) {
            val updatedStack = _undoStack.value + command
            _undoStack.value = if (updatedStack.size > 100) updatedStack.drop(updatedStack.size - 100) else updatedStack
        }
        _redoStack.value = emptyList()
    }

    @Synchronized
    override fun undo() {
        val command = _undoStack.value.lastOrNull() ?: return
        val current = context.projectFlow.value ?: return
        val newProject = try {
            command.undo(current)
        } catch (e: Exception) {
            scope.launch {
                context.eventFlow.emit(EditorEvent.ShowError(e.message ?: "Invalid undo"))
            }
            return
        }
        context.projectFlow.value = newProject
        _undoStack.value = _undoStack.value.dropLast(1)
        _redoStack.value = _redoStack.value + command
    }

    @Synchronized
    override fun redo() {
        val command = _redoStack.value.lastOrNull() ?: return
        val current = context.projectFlow.value ?: return
        val newProject = try {
            command.execute(current)
        } catch (e: Exception) {
            scope.launch {
                context.eventFlow.emit(EditorEvent.ShowError(e.message ?: "Invalid redo"))
            }
            return
        }
        context.projectFlow.value = newProject
        _redoStack.value = _redoStack.value.dropLast(1)
        _undoStack.value = _undoStack.value + command
    }

    override fun close() {
        scope.cancel()
    }
}