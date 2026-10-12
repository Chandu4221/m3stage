package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
}
