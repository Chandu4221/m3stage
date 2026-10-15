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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(InternalComposeUiApi::class)
class MultiSelectTest {

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
    fun testSelectionDelegateSetOperations() {
        val delegate = SelectionDelegate()
        val n1 = NodeId("n1")
        val n2 = NodeId("n2")
        val n3 = NodeId("n3")

        assertNull(delegate.selectedNodeId.value)
        assertTrue(delegate.selectedNodeIds.value.isEmpty())

        // Single select
        delegate.selectNode(n1)
        assertEquals(setOf(n1), delegate.selectedNodeIds.value)
        assertEquals(n1, delegate.selectedNodeId.value)

        // Toggle add n2 (Shift-click)
        delegate.toggleNodeInSelection(n2)
        assertEquals(setOf(n1, n2), delegate.selectedNodeIds.value)
        assertEquals(n2, delegate.selectedNodeId.value)

        // Toggle add n3
        delegate.toggleNodeInSelection(n3)
        assertEquals(setOf(n1, n2, n3), delegate.selectedNodeIds.value)
        assertEquals(n3, delegate.selectedNodeId.value)

        // Toggle remove n2
        delegate.toggleNodeInSelection(n2)
        assertEquals(setOf(n1, n3), delegate.selectedNodeIds.value)
        assertEquals(n3, delegate.selectedNodeId.value)

        // Plain select replaces set
        delegate.selectNode(n2)
        assertEquals(setOf(n2), delegate.selectedNodeIds.value)
        assertEquals(n2, delegate.selectedNodeId.value)

        // Select null clears set
        delegate.selectNode(null)
        assertTrue(delegate.selectedNodeIds.value.isEmpty())
        assertNull(delegate.selectedNodeId.value)
    }

    @Test
    fun testBatchDeleteWithSingleUndoRestoresOriginalPositions() {
        val store = createStore()
        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)

        val screen = store.project.value!!.findScreen(screenId)!!
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }

        // Add 3 buttons to column
        store.selectNode(col.id)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)

        val colAfterAdd = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val buttons = colAfterAdd.children.filter { it.kind == ComponentKind.Button }
        assertEquals(3, buttons.size)
        val b0 = buttons[0]
        val b1 = buttons[1]
        val b2 = buttons[2]

        // Select b0 and b2 (indices 1 and 3, assuming placeholder text is index 0)
        store.selectNodes(setOf(b0.id, b2.id))
        assertEquals(2, store.selectedNodeIds.value.size)

        // Batch delete
        store.removeSelectedNodes()
        assertTrue(store.selectedNodeIds.value.isEmpty())

        val colAfterDelete = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val remainingButtons = colAfterDelete.children.filter { it.kind == ComponentKind.Button }
        assertEquals(1, remainingButtons.size)
        assertEquals(b1.id, remainingButtons[0].id)

        // Single undo step restores both
        store.undo()

        val colAfterUndo = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val restoredButtons = colAfterUndo.children.filter { it.kind == ComponentKind.Button }
        assertEquals(3, restoredButtons.size)
        assertEquals(listOf(b0.id, b1.id, b2.id), restoredButtons.map { it.id })
    }

    @Test
    fun testBatchDuplicateCreatesFreshSubtreesAndSelectsCopies() {
        val store = createStore()
        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)

        val screen = store.project.value!!.findScreen(screenId)!!
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }

        store.selectNode(col.id)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)

        val colWithButtons = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val origButtons = colWithButtons.children.filter { it.kind == ComponentKind.Button }
        assertEquals(2, origButtons.size)

        // Select both buttons
        store.selectNodes(origButtons.map { it.id }.toSet())
        store.duplicateSelectedNodes()

        val colAfterDuplicate = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val allButtons = colAfterDuplicate.children.filter { it.kind == ComponentKind.Button }
        assertEquals(4, allButtons.size, "Should now have 4 buttons")

        // Selection should match the duplicated nodes
        assertEquals(2, store.selectedNodeIds.value.size)
        assertTrue(store.selectedNodeIds.value.none { it in origButtons.map { b -> b.id } }, "Selected IDs must be the new copies")

        // Undo removes both duplicates
        store.undo()
        val colAfterUndo = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val buttonsAfterUndo = colAfterUndo.children.filter { it.kind == ComponentKind.Button }
        assertEquals(2, buttonsAfterUndo.size)
        assertEquals(origButtons.map { it.id }, buttonsAfterUndo.map { it.id })
    }

    @Test
    fun testKeyboardRoutingMultiSelectDeleteAndDuplicate() {
        val store = createStore()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()
        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)

        val screen = store.project.value!!.findScreen(screenId)!!
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }

        store.selectNode(col.id)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)
        store.addNodeToActiveScreen(col.id, ComponentKind.Button)

        val colNodes = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        val buttons = colNodes.children.filter { it.kind == ComponentKind.Button }

        // Multi-select 2 buttons
        store.selectNodes(buttons.map { it.id }.toSet())
        assertEquals(2, store.selectedNodeIds.value.size)

        // Ctrl+D triggers batch duplicate
        val ctrlD = KeyEvent(Key.D, KeyEventType.KeyDown, isCtrlPressed = true)
        val handledDuplicate = handleEditorKeyEvent(ctrlD, store, panels, viewport)
        assertTrue(handledDuplicate)
        assertEquals(2, store.selectedNodeIds.value.size)

        val afterDup = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        assertEquals(4, afterDup.children.filter { it.kind == ComponentKind.Button }.size)

        // Delete key triggers batch delete on the currently selected 2 duplicated buttons
        val deleteKey = KeyEvent(Key.Delete, KeyEventType.KeyDown)
        val handledDelete = handleEditorKeyEvent(deleteKey, store, panels, viewport)
        assertTrue(handledDelete)
        assertTrue(store.selectedNodeIds.value.isEmpty())

        val afterDel = store.project.value!!.findScreen(screenId)!!.root.findNode(col.id)!!
        assertEquals(2, afterDel.children.filter { it.kind == ComponentKind.Button }.size)
    }

    @Test
    fun testEscapeKeyCascadeRegression() {
        val store = createStore()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()
        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)

        val screen = store.project.value!!.findScreen(screenId)!!
        store.addNodeToActiveScreen(screen.root.id, ComponentKind.Column)
        val col = store.project.value!!.findScreen(screenId)!!.root.allDirectChildren.first { it.kind == ComponentKind.Column }
        store.selectNode(col.id)

        val escapeKey = KeyEvent(Key.Escape, KeyEventType.KeyDown)

        // 1st Escape: pops selection anchor / deselects node
        val handled1 = handleEditorKeyEvent(escapeKey, store, panels, viewport)
        assertTrue(handled1)
        assertNull(store.selectedNodeId.value)
        assertTrue(store.selectedNodeIds.value.isEmpty())

        // 2nd Escape: deselects screen
        val handled2 = handleEditorKeyEvent(escapeKey, store, panels, viewport)
        assertTrue(handled2)
        assertNull(store.activeScreenId.value)

        // 3rd Escape: no active screen or node, returns false
        val handled3 = handleEditorKeyEvent(escapeKey, store, panels, viewport)
        assertEquals(false, handled3)
    }
}
