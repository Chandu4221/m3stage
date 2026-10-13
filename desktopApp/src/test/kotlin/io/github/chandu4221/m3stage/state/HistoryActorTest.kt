package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryActorTest {

    private class TestIdGen : IdGenerator {
        private var count = 0
        override fun nextProjectId(): ProjectId = ProjectId("p-${++count}")
        override fun nextScreenId(): ScreenId = ScreenId("s-${++count}")
        override fun nextNodeId(): NodeId = NodeId("n-${++count}")
    }

    private class DummyRepo : ProjectRepository {
        override suspend fun save(project: Project) {}
        override suspend fun load(): Project? = null
    }

    private class DummyCodeGen : CodeGenerator {
        override fun generate(project: Project): GeneratedProject = GeneratedProject(emptyList())
    }

    @Test
    fun testRapidFire100MutationsAndUndos() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val screenId = ScreenId("screen-1")
        val initialRoot = DesignNode(
            id = NodeId("root-col"),
            kind = ComponentKind.Column
        )
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "TestProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(id = screenId, name = "Main", route = "/", root = initialRoot)
            )
        )

        val context = EditorContext(idGen, DummyRepo(), DummyCodeGen())
        context.projectFlow.value = initialProject

        val history = HistoryDelegate(context, testScope)

        val initialChildCount = context.projectFlow.value!!.screens.first().root.children.size
        assertEquals(0, initialChildCount)

        // Rapid-fire 100 AddNodeCommands in a tight loop
        for (i in 1..100) {
            val node = DesignNode(
                id = NodeId("child-$i"),
                kind = ComponentKind.Button
            )
            history.execute(AddNodeCommand(screenId, initialRoot.id, node))
        }

        testScope.advanceUntilIdle()

        val finalProject = context.projectFlow.value!!
        val currentScreen = finalProject.screens.first()
        assertEquals(100, currentScreen.root.children.size, "Expected exactly 100 nodes added")
        assertEquals(100, history.undoStack.value.size, "Expected 100 undo stack entries")

        // Undo 100 times in a tight loop
        for (i in 1..100) {
            history.undo()
        }

        testScope.advanceUntilIdle()

        val restoredProject = context.projectFlow.value!!
        val restoredScreen = restoredProject.screens.first()
        assertEquals(0, restoredScreen.root.children.size, "Expected 0 nodes after 100 undos")
        assertEquals(0, history.undoStack.value.size, "Expected empty undo stack")
        assertEquals(100, history.redoStack.value.size, "Expected 100 redo stack entries")

        history.close()
    }

    @Test
    fun testUpdateModifierCoalescing() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val screenId = ScreenId("screen-1")
        val nodeId = NodeId("box-1")
        val initialRoot = DesignNode(
            id = nodeId,
            kind = ComponentKind.Box,
            modifiers = listOf(io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding.all(0f))
        )
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "TestProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(id = screenId, name = "Main", route = "/", root = initialRoot)
            )
        )

        val context = EditorContext(idGen, DummyRepo(), DummyCodeGen())
        context.projectFlow.value = initialProject

        val history = HistoryDelegate(context, testScope)

        // Rapid updates to the same modifier within 300ms
        history.execute(
            UpdateModifierCommand(
                screenId = screenId,
                nodeId = nodeId,
                index = 0,
                newModifier = io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding.all(1f)
            )
        )
        history.execute(
            UpdateModifierCommand(
                screenId = screenId,
                nodeId = nodeId,
                index = 0,
                newModifier = io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding.all(12f)
            )
        )
        history.execute(
            UpdateModifierCommand(
                screenId = screenId,
                nodeId = nodeId,
                index = 0,
                newModifier = io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding.all(123f)
            )
        )

        testScope.advanceUntilIdle()

        // Should have coalesced to 1 undo entry
        assertEquals(1, history.undoStack.value.size, "Rapid modifier updates should coalesce into 1 undo entry")

        val currentMod = (context.projectFlow.value!!.screens.first().root.modifiers.first() as io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding).start
        assertEquals(123f, currentMod)

        // Single undo should restore back to pre-edit value (0f)
        history.undo()
        testScope.advanceUntilIdle()

        val undoneMod = (context.projectFlow.value!!.screens.first().root.modifiers.first() as io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding).start
        assertEquals(0f, undoneMod, "Single undo should restore to pre-edit value")
        assertEquals(0, history.undoStack.value.size)

        history.close()
    }

    @Test
    fun testDifferentModifierTypesAtSameIndexDoNotCoalesce() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val screenId = ScreenId("screen-1")
        val nodeId = NodeId("box-1")
        val initialRoot = DesignNode(
            id = nodeId,
            kind = ComponentKind.Box,
            modifiers = listOf(io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding.all(0f))
        )
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "TestProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(id = screenId, name = "Main", route = "/", root = initialRoot)
            )
        )

        val context = EditorContext(idGen, DummyRepo(), DummyCodeGen())
        context.projectFlow.value = initialProject

        val history = HistoryDelegate(context, testScope)

        // 1. Update index 0 to a Padding modifier
        history.execute(
            UpdateModifierCommand(
                screenId = screenId,
                nodeId = nodeId,
                index = 0,
                newModifier = io.github.chandu4221.m3stage.modifier.ModifierSpec.Padding.all(8f)
            )
        )

        // 2. Immediately update index 0 to a FillMaxWidth modifier (different type) within 300ms
        history.execute(
            UpdateModifierCommand(
                screenId = screenId,
                nodeId = nodeId,
                index = 0,
                newModifier = io.github.chandu4221.m3stage.modifier.ModifierSpec.FillMaxWidth(1f)
            )
        )

        testScope.advanceUntilIdle()

        // Since the modifier types differ, they must NOT coalesce
        assertEquals(2, history.undoStack.value.size, "Different modifier types at the same index must not coalesce")

        history.close()
    }

    @Test
    fun testUndoStackCap100() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val screenId = ScreenId("screen-1")
        val initialRoot = DesignNode(
            id = NodeId("root-col"),
            kind = ComponentKind.Column
        )
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "TestProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(id = screenId, name = "Main", route = "/", root = initialRoot)
            )
        )

        val context = EditorContext(idGen, DummyRepo(), DummyCodeGen())
        context.projectFlow.value = initialProject

        val history = HistoryDelegate(context, testScope)

        // Push 150 commands
        for (i in 1..150) {
            val node = DesignNode(
                id = NodeId("child-$i"),
                kind = ComponentKind.Button
            )
            history.execute(AddNodeCommand(screenId, initialRoot.id, node))
        }

        testScope.advanceUntilIdle()

        // Stack size must be capped at 100
        assertEquals(100, history.undoStack.value.size, "Undo stack should be capped at 100")

        history.close()
    }

    @Test
    fun testCommandExecutesSynchronouslyWithoutAdvancingScheduler() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val screenId = ScreenId("screen-1")
        val initialRoot = DesignNode(id = NodeId("root"), kind = ComponentKind.Column)
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "TestProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(id = screenId, name = "Main", route = "/", root = initialRoot, canvasX = 0f, canvasY = 0f)
            )
        )

        val context = EditorContext(idGen, DummyRepo(), DummyCodeGen())
        context.projectFlow.value = initialProject

        val history = HistoryDelegate(context, testScope)

        // Execute MoveScreenCommand
        history.execute(MoveScreenCommand(screenId, 150f, 250f))

        // Assert projectFlow was updated synchronously without calling advanceUntilIdle()
        val screen = context.projectFlow.value!!.screens.first()
        assertEquals(150f, screen.canvasX, "Position must update synchronously on the caller thread")
        assertEquals(250f, screen.canvasY, "Position must update synchronously on the caller thread")
        assertEquals(1, history.undoStack.value.size)

        // Verify undo also applies synchronously without advancing scheduler
        history.undo()
        val undoneScreen = context.projectFlow.value!!.screens.first()
        assertEquals(0f, undoneScreen.canvasX, "Undo must update position synchronously")
        assertEquals(0f, undoneScreen.canvasY, "Undo must update position synchronously")
        assertEquals(0, history.undoStack.value.size)

        history.close()
    }

    @Test
    fun testCommandEmitsErrorWhenNoActiveProject() = runTest {
        val idGen = TestIdGen()
        val context = EditorContext(idGen, DummyRepo(), DummyCodeGen())
        context.projectFlow.value = null

        val history = HistoryDelegate(context, this)

        val receivedEvents = mutableListOf<EditorEvent>()
        val job = launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            context.eventFlow.collect { receivedEvents.add(it) }
        }

        history.execute(MoveScreenCommand(ScreenId("s-1"), 10f, 10f))
        testScheduler.advanceUntilIdle()

        assertEquals(1, receivedEvents.size)
        val errorEvent = receivedEvents.first() as EditorEvent.ShowError
        assertEquals("No active project to execute command on", errorEvent.message)

        job.cancel()
    }
}

