package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NodeRenameTest {

    private class TestIdGen : IdGenerator {
        private var count = 0
        override fun nextProjectId(): ProjectId = ProjectId("p-${++count}")
        override fun nextScreenId(): ScreenId = ScreenId("s-${++count}")
        override fun nextNodeId(): NodeId = NodeId("n-${++count}")
    }

    private fun createStore(): EditorStore {
        val idGen = TestIdGen()
        val repo = object : ProjectRepository {
            override suspend fun save(project: Project) {}
            override suspend fun load(): Project? = null
        }
        val codeGen = object : CodeGenerator {
            override fun generate(project: Project): GeneratedProject = GeneratedProject(emptyList())
        }
        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()
        return store
    }

    @Test
    fun testRenameNodeRoundTripAndUndoRedo() {
        val store = createStore()
        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)

        val screen = store.project.value!!.findScreen(screenId)!!
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }

        // Initial label should be null and display fallback to displayName
        assertNull(col.label)
        assertEquals("Column", col.label ?: col.kind.displayName)

        // Rename to "Main Content Flow"
        store.renameNode(col.id, "Main Content Flow")

        val renamedCol = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        assertEquals("Main Content Flow", renamedCol.label)
        assertEquals("Main Content Flow", renamedCol.label ?: renamedCol.kind.displayName)

        // Undo restores null
        store.undo()
        val undoneCol = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        assertNull(undoneCol.label)
        assertEquals("Column", undoneCol.label ?: undoneCol.kind.displayName)

        // Redo reapplies label
        store.redo()
        val redoneCol = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        assertEquals("Main Content Flow", redoneCol.label)
    }

    @Test
    fun testBlankOrWhitespaceInputResetsLabelToNull() {
        val store = createStore()
        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)

        val screen = store.project.value!!.findScreen(screenId)!!
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }

        // Set initial label
        store.renameNode(col.id, "Sidebar")
        assertEquals("Sidebar", store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!.label)

        // Rename with blank string "   "
        store.renameNode(col.id, "   ")
        val resetCol = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        assertNull(resetCol.label, "Blank whitespace must reset label to null")
        assertEquals("Column", resetCol.label ?: resetCol.kind.displayName)
    }
}
