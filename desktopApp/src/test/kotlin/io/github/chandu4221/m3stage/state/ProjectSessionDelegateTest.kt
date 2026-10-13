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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectSessionDelegateTest {

    private class TestIdGen : IdGenerator {
        private var count = 0
        override fun nextProjectId(): ProjectId = ProjectId("p-${++count}")
        override fun nextScreenId(): ScreenId = ScreenId("s-${++count}")
        override fun nextNodeId(): NodeId = NodeId("n-${++count}")
    }

    private class DummyCodeGen : CodeGenerator {
        override fun generate(project: Project): GeneratedProject = GeneratedProject(emptyList())
    }

    private class MockProjectRepo(
        var loadException: Exception? = null,
        var projectToLoad: Project? = null
    ) : ProjectRepository {
        var saveCallCount = 0
        var lastSavedProject: Project? = null

        override suspend fun save(project: Project) {
            saveCallCount++
            lastSavedProject = project
        }

        override suspend fun load(): Project? {
            loadException?.let { throw it }
            return projectToLoad
        }
    }

    @Test
    fun testFailedLoadBlocksAutosaveFromOverwritingDisk() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val repo = MockProjectRepo(loadException = java.io.IOException("File locked"))
        val context = EditorContext(TestIdGen(), repo, DummyCodeGen())

        val delegate = ProjectSessionDelegate(context, testScope)

        // 1. Trigger load which will fail
        delegate.loadProject()
        testScope.advanceUntilIdle()

        assertTrue(delegate.autosaveBlocked, "autosaveBlocked must be true after load failure")
        assertEquals(0, repo.saveCallCount, "No save must be called during load failure")

        // 2. Advance time past the 800ms debounce
        testScope.advanceTimeBy(1000)
        testScope.advanceUntilIdle()

        assertEquals(0, repo.saveCallCount, "Autosave must NOT overwrite disk after failed load")

        // 3. Even if projectFlow mutates (e.g. user edits fallback project), autosave stays blocked
        context.projectFlow.value = context.projectFlow.value?.copy(name = "Changed")
        testScope.advanceTimeBy(1000)
        testScope.advanceUntilIdle()

        assertEquals(0, repo.saveCallCount, "Autosave must remain blocked until manual save")

        // 4. Explicit manual save unblocks
        delegate.saveProject()
        testScope.advanceUntilIdle()

        assertEquals(1, repo.saveCallCount, "Explicit manual save should write to disk")
        assertFalse(delegate.autosaveBlocked, "Explicit save must unblock autosave")
    }

    @Test
    fun testSuccessfulLoadAllowsSubsequentAutosave() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "LoadedProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(
                    id = ScreenId("screen-1"),
                    name = "Home",
                    route = "/",
                    root = DesignNode(id = NodeId("root"), kind = ComponentKind.Scaffold)
                )
            )
        )
        val repo = MockProjectRepo(projectToLoad = initialProject)
        val context = EditorContext(idGen, repo, DummyCodeGen())

        val delegate = ProjectSessionDelegate(context, testScope)

        delegate.loadProject()
        testScope.advanceUntilIdle()

        assertFalse(delegate.autosaveBlocked)
        assertEquals("LoadedProject", context.projectFlow.value?.name)
        assertEquals(0, repo.saveCallCount)

        // Mutate projectFlow -> should trigger debounced autosave
        context.projectFlow.value = initialProject.copy(name = "UpdatedProject")
        testScope.advanceTimeBy(1000)
        testScope.advanceUntilIdle()

        assertEquals(1, repo.saveCallCount)
        assertEquals("UpdatedProject", repo.lastSavedProject?.name)
    }
}
