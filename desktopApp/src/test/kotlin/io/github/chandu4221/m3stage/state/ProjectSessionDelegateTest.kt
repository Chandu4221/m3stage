package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        var projectToLoad: Project? = null,
        var onSaveDelayMs: Long = 0,
        var onLoadDelayMs: Long = 0
    ) : ProjectRepository {
        var saveCallCount = 0
        var concurrentSaveDetected = false
        private var isSaving = false
        var lastSavedProject: Project? = null

        override suspend fun save(project: Project) {
            if (isSaving) {
                concurrentSaveDetected = true
            }
            isSaving = true
            try {
                if (onSaveDelayMs > 0) {
                    delay(onSaveDelayMs)
                }
                saveCallCount++
                lastSavedProject = project
            } finally {
                isSaving = false
            }
        }

        override suspend fun load(): Project? {
            if (onLoadDelayMs > 0) {
                delay(onLoadDelayMs)
            }
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

    @Test
    fun testFlushSaveWritesDirtyProjectSynchronously() = runTest {
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

        // Mutate projectFlow -> dirty, but do NOT advance time past 800ms debounce
        context.projectFlow.value = initialProject.copy(name = "ChangedImmediately")
        testScheduler.runCurrent()

        assertEquals(0, repo.saveCallCount, "Should not be saved before debounce expires")
        assertTrue(delegate.isDirty.value, "Must be dirty")

        // Trigger flushSave (e.g. on exit)
        delegate.flushSave()

        assertEquals(1, repo.saveCallCount, "flushSave must immediately write to disk")
        assertEquals("ChangedImmediately", repo.lastSavedProject?.name)
        assertFalse(delegate.isDirty.value, "isDirty must be cleared after flushSave")
    }

    @Test
    fun testProjectSessionDelegateCloseCancelsScopeAndPreventsAutosave() = runTest {
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

        // Close session delegate
        delegate.close()

        // Mutate projectFlow -> dirty
        context.projectFlow.value = initialProject.copy(name = "MutatedAfterClose")
        testScope.advanceTimeBy(2000)
        testScope.advanceUntilIdle()

        assertEquals(0, repo.saveCallCount, "No autosaves should run after session delegate is closed")
    }

    @Test
    fun testConcurrentSavesAreSerializedByMutex() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        val initialProject = Project(
            id = ProjectId("proj-1"),
            name = "Project1",
            basePackage = "com.example.test",
            screens = emptyList()
        )
        val repo = MockProjectRepo(projectToLoad = initialProject, onSaveDelayMs = 500)
        val context = EditorContext(idGen, repo, DummyCodeGen())

        val delegate = ProjectSessionDelegate(context, testScope)
        delegate.loadProject()
        testScope.advanceUntilIdle()

        // Trigger two manual saves in quick succession while onSaveDelayMs is active
        context.projectFlow.value = initialProject.copy(name = "Edit1")
        delegate.saveProject()
        context.projectFlow.value = initialProject.copy(name = "Edit2")
        delegate.saveProject()

        testScope.advanceUntilIdle()

        assertFalse(repo.concurrentSaveDetected, "Concurrent saves must never interleave")
        assertEquals(2, repo.saveCallCount, "Both saves should complete serialized")
    }

    @Test
    fun testStaleLoadDoesNotOverwriteUserModifications() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        fun createScreen(id: String, name: String) = Screen(
            id = ScreenId(id),
            name = name,
            route = "/$name",
            root = DesignNode(id = NodeId("r-$id"), kind = ComponentKind.Scaffold)
        )
        val diskProject = Project(
            id = ProjectId("disk-1"),
            name = "DiskProject",
            basePackage = "com.disk",
            screens = listOf(createScreen("s-disk", "ScreenDisk"))
        )
        val repo = MockProjectRepo(projectToLoad = diskProject, onLoadDelayMs = 1000)
        val context = EditorContext(idGen, repo, DummyCodeGen())

        val events = mutableListOf<EditorEvent>()
        testScope.backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            context.eventFlow.collect { events.add(it) }
        }

        val delegate = ProjectSessionDelegate(context, testScope)

        // Initial project in memory
        val initialMemoryProject = Project(
            id = ProjectId("init-1"),
            name = "InitialMemory",
            basePackage = "com.init",
            screens = listOf(createScreen("s-init", "ScreenInit"))
        )
        context.projectFlow.value = initialMemoryProject

        // Start loading from disk
        delegate.loadProject()
        testScheduler.runCurrent()

        // User edits the project while load is in flight
        val userEditedProject = initialMemoryProject.copy(name = "UserEditedDuringLoad")
        context.projectFlow.value = userEditedProject

        // Complete the in-flight load
        testScope.advanceUntilIdle()

        // The loaded disk project must NOT overwrite the user's modifications
        assertEquals(userEditedProject, context.projectFlow.value)
        assertTrue(events.any { it is EditorEvent.LoadFailed && it.reason.contains("modified while loading") })
    }

    @Test
    fun testNewerLoadCancelsOlderInFlightLoad() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)

        val idGen = TestIdGen()
        fun createScreen(id: String, name: String) = Screen(
            id = ScreenId(id),
            name = name,
            route = "/$name",
            root = DesignNode(id = NodeId("r-$id"), kind = ComponentKind.Scaffold)
        )
        val slowProject = Project(
            id = ProjectId("p-slow"),
            name = "SlowProject",
            basePackage = "com.slow",
            screens = listOf(createScreen("s-slow", "ScreenSlow"))
        )
        val fastProject = Project(
            id = ProjectId("p-fast"),
            name = "FastProject",
            basePackage = "com.fast",
            screens = listOf(createScreen("s-fast", "ScreenFast"))
        )

        var returnProject = slowProject
        var delayMs = 1000L
        val repo = object : ProjectRepository {
            override suspend fun save(project: Project) {}
            override suspend fun load(): Project? {
                delay(delayMs)
                return returnProject
            }
        }
        val context = EditorContext(idGen, repo, DummyCodeGen())
        val delegate = ProjectSessionDelegate(context, testScope)

        // 1. Trigger first slow load
        returnProject = slowProject
        delayMs = 1000L
        delegate.loadProject()
        testScheduler.runCurrent()

        // 2. Trigger second faster load
        returnProject = fastProject
        delayMs = 200L
        delegate.loadProject()

        // Advance 300ms -> fast load finishes
        testScope.advanceTimeBy(300)
        assertEquals("FastProject", context.projectFlow.value?.name)

        // Advance remaining time -> slow load would finish, but its loadId is stale
        testScope.advanceUntilIdle()
        assertEquals("FastProject", context.projectFlow.value?.name, "Stale load must not overwrite newer load")
    }
}

