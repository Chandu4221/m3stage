package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.flatten
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClipboardTest {

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
    fun testCopyAndPasteTwiceYieldsUniqueIds() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)

            val screen = store.project.value!!.findScreen(screenId)!!
            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)

            val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }
            store.selectNode(col.id)
            store.addNodeToActiveScreen(col.id, ComponentKind.Button)

            val updatedCol = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            val button = updatedCol.children.first { it.kind == ComponentKind.Button }

            // Copy button
            store.copyNode(button.id)
            assertNotNull(store.clipboardDelegate.clip.value)

            // Paste 1st time
            store.selectNode(col.id)
            store.pasteNode()

            // Paste 2nd time
            store.selectNode(col.id)
            store.pasteNode()

            val finalCol = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            val buttons = finalCol.children.filter { it.kind == ComponentKind.Button }
            assertEquals(3, buttons.size, "Should have 1 original + 2 pasted buttons")

            val allIds = store.project.value!!.findScreen(screenId)!!.root.flatten().map { it.id }
            assertEquals(allIds.size, allIds.toSet().size, "All node IDs in screen must be strictly unique")
        } finally {
            store.close()
        }
    }

    @Test
    fun testCutAndUndoRestoresNodeWhileClipboardSurvives() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)

            val screen = store.project.value!!.findScreen(screenId)!!
            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
            val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }
            val text = col.children.first()

            // Cut text
            store.cutNode(text.id)

            val afterCut = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            assertTrue(afterCut.children.none { it.id == text.id }, "Cut node must be removed from parent")
            assertNotNull(store.clipboardDelegate.clip.value, "Clipboard must hold cut node")

            // Undo cut (which undid the RemoveNodeCommand)
            store.undo()

            val afterUndo = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            assertTrue(afterUndo.children.any { it.id == text.id }, "Undo must restore the cut node")
            assertNotNull(store.clipboardDelegate.clip.value, "Clipboard must survive undo")
        } finally {
            store.close()
        }
    }

    @Test
    fun testCrossScreenPaste() {
        val store = createStore()
        try {
            val screen1Id = store.project.value!!.screens.first().id
            store.setActiveScreen(screen1Id)

            val screen1 = store.project.value!!.findScreen(screen1Id)!!
            store.addNodeToActiveScreen(screen1.root.id, ComponentKind.Column)
            val col1 = store.project.value!!.findScreen(screen1Id)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }
            val text1 = col1.children.first()

            // Copy from screen 1
            store.copyNode(text1.id)

            // Add screen 2 and add a Column to it
            store.addNewScreen("Screen2", "/screen2")
            val screen2Id = store.project.value!!.screens.first { it.name == "Screen2" }.id
            store.setActiveScreen(screen2Id)
            val screen2 = store.project.value!!.findScreen(screen2Id)!!
            store.addNodeToActiveScreen(screen2.root.id, ComponentKind.Column)
            val col2 = store.project.value!!.findScreen(screen2Id)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }

            // Paste into screen 2
            store.selectNode(col2.id)
            store.pasteNode()

            val afterPasteScreen2 = store.project.value!!.findScreen(screen2Id)!!.root.findNode(col2.id)!!
            assertEquals(2, afterPasteScreen2.children.size, "Screen 2 column must have its original child + pasted child")
        } finally {
            store.close()
        }
    }

    @Test
    fun testSingleUndoPerPaste() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)

            val screen = store.project.value!!.findScreen(screenId)!!
            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
            val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }
            val text = col.children.first()

            store.copyNode(text.id)
            store.selectNode(col.id)
            store.pasteNode()

            val afterPaste = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            assertEquals(2, afterPaste.children.size)

            // A single undo should remove the pasted node
            store.undo()

            val afterUndo = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            assertEquals(1, afterUndo.children.size)
        } finally {
            store.close()
        }
    }
}
