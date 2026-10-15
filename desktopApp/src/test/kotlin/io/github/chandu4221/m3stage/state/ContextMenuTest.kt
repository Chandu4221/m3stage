package io.github.chandu4221.m3stage.state

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.query.flatten
import io.github.chandu4221.m3stage.ui.page.EditorShortcutActions
import io.github.chandu4221.m3stage.ui.page.handleEditorKeyEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(InternalComposeUiApi::class)
class ContextMenuTest {

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
    fun testSelectAllNodesSelectsAllVisibleNodes() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)
            val screen = store.project.value!!.findScreen(screenId)!!

            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
            val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }
            store.addNodeToActiveScreen(col.id, ComponentKind.Button)
            store.addNodeToActiveScreen(col.id, ComponentKind.Text)

            val updatedScreen = store.project.value!!.findScreen(screenId)!!
            val textNode = updatedScreen.root.allDirectChildren.first { it.kind == ComponentKind.Column }
                .allDirectChildren.first { it.kind == ComponentKind.Text }

            // Hide text node
            store.toggleVisibility(textNode.id)
            val screenWithHidden = store.project.value!!.findScreen(screenId)!!

            // Select All should select visible nodes only
            store.selectAllNodes(screenWithHidden)

            val selectedIds = store.selectedNodeIds.value
            assertFalse(selectedIds.contains(textNode.id), "Hidden node must not be in select-all set")
            assertTrue(selectedIds.contains(col.id))
            assertTrue(selectedIds.contains(screenWithHidden.root.id))
            assertEquals(
                screenWithHidden.root.flatten().filter { it.isVisible }.map { it.id }.toSet(),
                selectedIds
            )
        } finally {
            store.close()
        }
    }

    @Test
    fun testContextMenuActionsOnSingleNode() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)
            val screen = store.project.value!!.findScreen(screenId)!!

            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
            val col = store.project.value!!.findScreen(screenId)!!.root.flatten().first { it.kind == ComponentKind.Column }
            store.selectNode(col.id)

            store.addNodeToActiveScreen(col.id, ComponentKind.Button)
            val btn = store.project.value!!.findScreen(screenId)!!.root.flatten().first { it.kind == ComponentKind.Button }

            // 1. Copy
            store.copyNode(btn.id)
            assertNotNull(store.clipboardDelegate.clip.value)
            assertEquals(btn.kind, store.clipboardDelegate.clip.value!!.kind)

            // 2. Lock / Unlock
            assertFalse(store.isNodeLocked(btn.id))
            store.toggleLock(btn.id)
            assertTrue(store.isNodeLocked(btn.id))
            store.toggleLock(btn.id)
            assertFalse(store.isNodeLocked(btn.id))

            // 3. Hide / Show
            store.toggleVisibility(btn.id)
            assertFalse(store.project.value!!.findScreen(screenId)!!.root.findNode(btn.id)!!.isVisible)
            store.toggleVisibility(btn.id)
            assertTrue(store.project.value!!.findScreen(screenId)!!.root.findNode(btn.id)!!.isVisible)

            // 4. Duplicate
            store.duplicateNode(btn.id)
            val buttonsAfterDup = store.project.value!!.findScreen(screenId)!!.root.flatten().filter { it.kind == ComponentKind.Button }
            assertEquals(2, buttonsAfterDup.size)

            // 5. Delete & Undo
            val dupId = buttonsAfterDup.last().id
            store.removeNode(dupId)
            assertEquals(1, store.project.value!!.findScreen(screenId)!!.root.flatten().filter { it.kind == ComponentKind.Button }.size)
            store.undo()
            assertEquals(2, store.project.value!!.findScreen(screenId)!!.root.flatten().filter { it.kind == ComponentKind.Button }.size)

            // 6. Cut & Undo
            store.cutNode(btn.id)
            assertNotNull(store.clipboardDelegate.clip.value)
            assertNull(store.project.value!!.findScreen(screenId)!!.root.findNode(btn.id))
            store.undo()
            assertNotNull(store.project.value!!.findScreen(screenId)!!.root.findNode(btn.id))
        } finally {
            store.close()
        }
    }

    @Test
    fun testContextMenuBatchActionsOnMultiSelection() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)
            val screen = store.project.value!!.findScreen(screenId)!!

            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
            val col = store.project.value!!.findScreen(screenId)!!.root.flatten().first { it.kind == ComponentKind.Column }
            store.selectNode(col.id)

            store.addNodeToActiveScreen(col.id, ComponentKind.Button)
            store.addNodeToActiveScreen(col.id, ComponentKind.Button)
            store.addNodeToActiveScreen(col.id, ComponentKind.Button)

            val colAfterAdd = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            val buttons = colAfterAdd.children.filter { it.kind == ComponentKind.Button }
            assertEquals(3, buttons.size)

            // Select all 3 buttons
            store.selectNodes(buttons.map { it.id }.toSet())
            assertEquals(3, store.selectedNodeIds.value.size)

            // Batch delete
            store.removeSelectedNodes()
            val colAfterDelete = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
            assertEquals(0, colAfterDelete.children.filter { it.kind == ComponentKind.Button }.size)
            assertTrue(store.selectedNodeIds.value.isEmpty())

            // Single undo restores all 3 in exact order
            store.undo()
            val restoredButtons = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!.children.filter { it.kind == ComponentKind.Button }
            assertEquals(3, restoredButtons.size)
            assertEquals(buttons.map { it.id }, restoredButtons.map { it.id })
        } finally {
            store.close()
        }
    }

    @Test
    fun testCrossScreenCopyPasteQAWorkflow() {
        val store = createStore()
        try {
            val screenAId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenAId)
            val screenA = store.project.value!!.findScreen(screenAId)!!

            store.addNodeToActiveScreen(screenA.root.id, ComponentKind.Column)
            val colA = store.project.value!!.findScreen(screenAId)!!.root.flatten().first { it.kind == ComponentKind.Column }
            store.selectNode(colA.id)

            // Add Button on Screen A
            store.addNodeToActiveScreen(colA.id, ComponentKind.Button)
            val btnA = store.project.value!!.findScreen(screenAId)!!.root.flatten().first { it.kind == ComponentKind.Button }

            // Right-click node -> Copy
            store.copyNode(btnA.id)
            assertNotNull(store.clipboardDelegate.clip.value)

            // Add Screen B
            store.addNewScreen("ScreenB", "screenB")
            val screenB = store.project.value!!.screens.first { it.name == "ScreenB" }
            store.setActiveScreen(screenB.id)

            // Right-click empty space on Screen B -> Paste
            store.pasteNode()

            val screenBButtons = store.project.value!!.findScreen(screenB.id)!!.root.flatten().filter { it.kind == ComponentKind.Button }
            assertEquals(1, screenBButtons.size)
            val pastedBtn = screenBButtons.first()
            assertEquals(ComponentKind.Button, pastedBtn.kind)
            assertTrue(pastedBtn.id != btnA.id, "Pasted node must have fresh unique ID")

            // Right-click empty space -> Deselect
            store.selectNode(null)
            assertTrue(store.selectedNodeIds.value.isEmpty())
            assertNull(store.selectedNodeId.value)
        } finally {
            store.close()
        }
    }

    @Test
    fun testKeyboardCtrlASelectAllShortcut() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)
            val screen = store.project.value!!.findScreen(screenId)!!

            store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
            val panels = ShellPanelsController()
            val viewport = CanvasViewportDelegate()

            val ctrlA = KeyEvent(Key.A, KeyEventType.KeyDown, isCtrlPressed = true)

            val handled = handleEditorKeyEvent(ctrlA, store, panels, viewport, EditorShortcutActions())
            assertTrue(handled)
            assertEquals(
                store.project.value!!.findScreen(screenId)!!.root.flatten().filter { it.isVisible }.map { it.id }.toSet(),
                store.selectedNodeIds.value
            )
        } finally {
            store.close()
        }
    }
}
