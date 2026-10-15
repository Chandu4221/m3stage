package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedFile
import io.github.chandu4221.m3stage.port.GeneratedProject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CodeGenDelegateTest {

    private fun createTestProject(screenName: String = "Home", contentMark: String = "v1"): Pair<Project, Screen> {
        val screen = Screen(
            id = ScreenId("s1"),
            name = screenName,
            route = "/$screenName",
            root = DesignNode(
                id = NodeId("n1"),
                kind = io.github.chandu4221.m3stage.component.ComponentKind.Column,
                props = emptyMap()
            )
        )
        val project = Project(
            id = ProjectId("p1"),
            name = "TestProject-$contentMark",
            basePackage = "com.example.test",
            screens = listOf(screen)
        )
        return project to screen
    }

    private class FakeCodeGenerator(
        var generateBlock: ((Project) -> GeneratedProject)? = null
    ) : CodeGenerator {
        var generateCount = 0
            private set
        var lastGeneratedProject: Project? = null
            private set

        override fun generate(project: Project): GeneratedProject {
            generateCount++
            lastGeneratedProject = project
            return generateBlock?.invoke(project) ?: run {
                val screen = project.screens.first()
                GeneratedProject(
                    files = listOf(
                        GeneratedFile(
                            packageName = project.basePackage,
                            fileName = "${screen.name}.kt",
                            content = "// Content for ${screen.name} from ${project.name}"
                        )
                    )
                )
            }
        }
    }

    @Test
    fun testDebounceTimingCoalescesRapidRequests() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val generator = FakeCodeGenerator()
        val delegate = CodeGenDelegate(
            codeGenerator = generator,
            scope = this,
            debounceMs = 500L,
            computationDispatcher = testDispatcher
        )

        val (proj1, screen1) = createTestProject("Home", "v1")
        val (proj2, screen2) = createTestProject("Home", "v2")

        // Initial state is Idle
        assertEquals(CodeGenUiState.Idle, delegate.previewState.value)

        // Request 1 at t=0
        delegate.request(screen1, proj1)
        assertTrue(delegate.previewState.value is CodeGenUiState.Generating)
        assertEquals(0, generator.generateCount)

        // Advance 300ms (t=300) -> should still be debounce-delayed
        advanceTimeBy(300)
        assertEquals(0, generator.generateCount)

        // Request 2 (rapid edit) at t=300
        delegate.request(screen2, proj2)
        advanceTimeBy(300) // t=600, 300ms since request 2
        assertEquals(0, generator.generateCount)

        // Advance another 201ms (t=801, 501ms since request 2)
        advanceTimeBy(201)
        assertEquals(1, generator.generateCount)
        assertEquals(proj2, generator.lastGeneratedProject)

        val state = delegate.previewState.value
        assertTrue(state is CodeGenUiState.Ready)
        assertEquals("Home.kt", state.fileName)
        assertTrue(state.content.contains("v2"))

        delegate.close()
    }

    @Test
    fun testTokenRaceConditionDiscardsStaleResult() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val (projA, screenA) = createTestProject("ScreenA", "vA")
        val (projB, screenB) = createTestProject("ScreenB", "vB")

        var allowScreenAGenerate = false
        val generator = FakeCodeGenerator { project ->
            val screen = project.screens.first()
            if (screen.name == "ScreenA") {
                // If ScreenA generation finishes after ScreenB
                GeneratedProject(
                    files = listOf(
                        GeneratedFile(project.basePackage, "ScreenA.kt", "// STALE ScreenA")
                    )
                )
            } else {
                GeneratedProject(
                    files = listOf(
                        GeneratedFile(project.basePackage, "ScreenB.kt", "// FRESH ScreenB")
                    )
                )
            }
        }

        val delegate = CodeGenDelegate(
            codeGenerator = generator,
            scope = this,
            debounceMs = 200L,
            computationDispatcher = testDispatcher
        )

        // Request screen A
        delegate.request(screenA, projA)
        advanceTimeBy(100)

        // Switch to screen B before screen A debounce finishes
        delegate.request(screenB, projB)
        advanceTimeBy(201) // Screen B finishes

        val stateAfterB = delegate.previewState.value
        assertTrue(stateAfterB is CodeGenUiState.Ready)
        assertEquals("ScreenB.kt", stateAfterB.fileName)
        assertEquals("// FRESH ScreenB", stateAfterB.content)

        // Screen A was superseded and its result was discarded
        delegate.close()
    }

    @Test
    fun testCloseCancelsInFlightAndResetsToIdle() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val generator = FakeCodeGenerator()
        val delegate = CodeGenDelegate(
            codeGenerator = generator,
            scope = this,
            debounceMs = 500L,
            computationDispatcher = testDispatcher
        )

        val (project, screen) = createTestProject("Dashboard", "v1")
        delegate.request(screen, project)
        assertTrue(delegate.previewState.value is CodeGenUiState.Generating)

        advanceTimeBy(200)
        delegate.close()

        assertEquals(CodeGenUiState.Idle, delegate.previewState.value)

        // Advance past debounce window -> generator must never be invoked
        advanceTimeBy(1000)
        assertEquals(0, generator.generateCount)
        assertEquals(CodeGenUiState.Idle, delegate.previewState.value)
    }

    @Test
    fun testGeneratorExceptionTransitionsToErrorState() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val failingGenerator = FakeCodeGenerator {
            throw IllegalStateException("Compilation failed: node unparseable")
        }
        val delegate = CodeGenDelegate(
            codeGenerator = failingGenerator,
            scope = this,
            debounceMs = 200L,
            computationDispatcher = testDispatcher
        )

        val (project, screen) = createTestProject("BrokenScreen", "v1")
        delegate.request(screen, project)

        advanceTimeBy(201)

        val state = delegate.previewState.value
        assertTrue(state is CodeGenUiState.Error)
        assertEquals("Compilation failed: node unparseable", state.message)

        delegate.close()
    }

    @Test
    fun testPreservesPreviousContentWhileGenerating() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val generator = FakeCodeGenerator()
        val delegate = CodeGenDelegate(
            codeGenerator = generator,
            scope = this,
            debounceMs = 200L,
            computationDispatcher = testDispatcher
        )

        val (proj1, screen1) = createTestProject("Home", "v1")
        delegate.request(screen1, proj1)
        advanceTimeBy(201)

        val readyState = delegate.previewState.value
        assertTrue(readyState is CodeGenUiState.Ready)
        val initialContent = readyState.content

        // Request v2
        val (proj2, screen2) = createTestProject("Home", "v2")
        delegate.request(screen2, proj2)

        // While generating, previous content should be preserved so UI doesn't flash blank
        val genState = delegate.previewState.value
        assertTrue(genState is CodeGenUiState.Generating)
        assertEquals(initialContent, genState.content)
        assertEquals("Home.kt", genState.fileName)

        advanceTimeBy(201)
        val newReadyState = delegate.previewState.value
        assertTrue(newReadyState is CodeGenUiState.Ready)
        assertTrue(newReadyState.content.contains("v2"))

        delegate.close()
    }
}
