package io.github.chandu4221.m3stage.ui.page

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.state.EditorStore
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.test.*

class DisallowedKindsTest {

    private class TestIdGen : IdGenerator {
        private var count = 0
        override fun nextProjectId(): ProjectId = ProjectId("p-${++count}")
        override fun nextScreenId(): ScreenId = ScreenId("s-${++count}")
        override fun nextNodeId(): NodeId = NodeId("n-${++count}")
    }

    private class DummyCodeGen : CodeGenerator {
        override fun generate(project: Project): GeneratedProject = GeneratedProject(emptyList())
    }

    private class MockProjectRepo : ProjectRepository {
        override suspend fun save(project: Project) {}
        override suspend fun load(): Project? = null
    }

    private val idGen = TestIdGen()
    private val repo = MockProjectRepo()
    private val codeGen = DummyCodeGen()

    @Test
    fun testDisallowedKindsChangesWithSelection() {
        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()

        val screen = store.project.value!!.screens.first()
        store.setActiveScreen(screen.id)

        // When root Scaffold is selected
        val rootDisallowed = computeDisallowedKinds(
            project = store.project.value,
            activeScreenId = screen.id,
            selectedNodeId = screen.root.id
        )

        // Scaffold allows TopAppBar, BottomAppBar, etc.
        assertFalse(ComponentKind.TopAppBar in rootDisallowed)

        // Add a Column inside Scaffold's content
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val updatedScreen = store.project.value!!.screens.first()
        val columnNode = updatedScreen.root.children.firstOrNull { it.kind == ComponentKind.Column }
            ?: updatedScreen.root.slots.values.flatten().first { it.kind == ComponentKind.Column }

        // Select the Column
        store.selectNode(columnNode.id)
        val columnDisallowed = computeDisallowedKinds(
            project = store.project.value,
            activeScreenId = screen.id,
            selectedNodeId = columnNode.id
        )

        // Column accepts Text, Button, etc.
        assertFalse(ComponentKind.Text in columnDisallowed)
        assertFalse(ComponentKind.Button in columnDisallowed)
    }

    @Test
    fun testDerivedStateOfRecomputesOnTheFourTriggers() {
        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()

        val initialScreen = store.project.value!!.screens.first()
        store.setActiveScreen(initialScreen.id)

        val projectState = mutableStateOf(store.project.value)
        val coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
        coroutineScope.launch {
            store.project.collect { projectState.value = it }
        }

        try {
            var computeCount = 0
            val derived = derivedStateOf {
                computeCount++
                computeDisallowedKinds(
                    project = projectState.value,
                    activeScreenId = store.activeScreenId.value,
                    selectedNodeId = store.selectedNodeId.value
                )
            }

            // Initial read triggers calculation
            val initial = derived.value
            assertEquals(1, computeCount)

            // Subsequent read without state change does NOT recompute
            val secondRead = derived.value
            assertSame(initial, secondRead)
            assertEquals(1, computeCount)

            // Trigger 1: Selection change -> recomputes
            val contentColumn = initialScreen.root.slots.values.flatten().first { it.kind == ComponentKind.Column }
            store.selectNode(contentColumn.id)
            val afterSelection = derived.value
            assertEquals(2, computeCount)

            // Trigger 2: Project structure change (add node) -> recomputes
            store.addNodeToActiveScreen(contentColumn.id, ComponentKind.Button)
            val afterAdd = derived.value
            assertEquals(3, computeCount)

            // Trigger 3: Undo -> recomputes
            store.undo()
            val afterUndo = derived.value
            assertEquals(4, computeCount)

            // Trigger 4: Redo -> recomputes
            store.redo()
            val afterRedo = derived.value
            assertEquals(5, computeCount)

            // Trigger 5: Active screen change -> recomputes
            store.addNewScreen("Screen2", "/screen2")
            val screen2 = store.project.value!!.screens.last()
            store.setActiveScreen(screen2.id)
            val afterScreenChange = derived.value
            assertEquals(6, computeCount)
        } finally {
            coroutineScope.cancel()
        }
    }

    @Test
    fun testNonStructuralStateChangesDoNotRecomputeDerivedState() {
        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()

        val projectState = mutableStateOf(store.project.value)
        val dummyDragState = mutableStateOf(0f)
        var computeCount = 0
        val derived = derivedStateOf {
            computeCount++
            computeDisallowedKinds(
                project = projectState.value,
                activeScreenId = store.activeScreenId.value,
                selectedNodeId = store.selectedNodeId.value
            )
        }

        val initial = derived.value
        assertEquals(1, computeCount)

        // Simulating 50 panel resize drag frames (drag state changes)
        for (i in 1..50) {
            dummyDragState.value = i * 2.5f
            val value = derived.value // read derivedState
            assertSame(initial, value)
        }

        // computeCount must remain 1 because panel drag is not in the derivedStateOf scope
        assertEquals(1, computeCount)
    }
}
