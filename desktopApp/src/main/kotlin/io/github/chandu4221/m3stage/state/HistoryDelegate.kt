package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistoryOp {
    data class Execute(val command: EditorCommand) : HistoryOp
    object Undo : HistoryOp
    object Redo : HistoryOp
}

class HistoryDelegate(
    private val context: EditorContext,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : HistoryState, AutoCloseable {

    private val _undoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    override val undoStack: StateFlow<List<EditorCommand>> = _undoStack.asStateFlow()

    private val _redoStack = MutableStateFlow<List<EditorCommand>>(emptyList())
    override val redoStack: StateFlow<List<EditorCommand>> = _redoStack.asStateFlow()

    private val commandQueue = Channel<HistoryOp>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (op in commandQueue) {
                when (op) {
                    is HistoryOp.Execute -> handleExecute(op.command)
                    is HistoryOp.Undo -> handleUndo()
                    is HistoryOp.Redo -> handleRedo()
                }
            }
        }
    }

    private var lastCoalescibleKey: Pair<Triple<io.github.chandu4221.m3stage.model.ScreenId, io.github.chandu4221.m3stage.model.NodeId, Int>, Long>? = null

    private suspend fun handleExecute(command: EditorCommand) {
        val base = context.projectFlow.value ?: return
        val newProject = try {
            command.execute(base)
        } catch (e: Exception) {
            context.eventFlow.emit(EditorEvent.ShowError(e.message ?: "Invalid operation"))
            return
        }

        val errors = ProjectValidation.validate(newProject)
        if (errors.isNotEmpty()) {
            context.eventFlow.emit(EditorEvent.ShowError(errors.first()))
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

    private fun handleUndo() {
        val command = _undoStack.value.lastOrNull() ?: return
        val current = context.projectFlow.value ?: return
        context.projectFlow.value = command.undo(current)
        _undoStack.value = _undoStack.value.dropLast(1)
        _redoStack.value = _redoStack.value + command
    }

    private fun handleRedo() {
        val command = _redoStack.value.lastOrNull() ?: return
        val current = context.projectFlow.value ?: return
        context.projectFlow.value = command.execute(current)
        _redoStack.value = _redoStack.value.dropLast(1)
        _undoStack.value = _undoStack.value + command
    }

    override fun execute(command: EditorCommand) {
        commandQueue.trySend(HistoryOp.Execute(command))
    }

    override fun undo() {
        commandQueue.trySend(HistoryOp.Undo)
    }

    override fun redo() {
        commandQueue.trySend(HistoryOp.Redo)
    }

    override fun close() {
        commandQueue.close()
        scope.cancel()
    }
}