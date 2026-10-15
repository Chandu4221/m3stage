package io.github.chandu4221.m3stage.state

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import io.github.chandu4221.m3stage.adapter.renderer.selection.DragReorderHelper
import io.github.chandu4221.m3stage.adapter.renderer.selection.toTreeInfo
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.query.findNode
import io.github.chandu4221.m3stage.ui.page.EditorShortcutActions
import io.github.chandu4221.m3stage.ui.page.handleEditorKeyEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(InternalComposeUiApi::class)
class DirectManipulationTest {

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

    private fun setupStoreWithThreeChildren(): Triple<EditorStore, ScreenId, List<NodeId>> {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            store.setActiveScreen(screenId)
            val screen = store.project.value!!.screens.first { it.id == screenId }

            val colId = NodeId("col-1")
            val childA = NodeId("child-A")
            val childB = NodeId("child-B")
            val childC = NodeId("child-C")

            val column = DesignNode(
                id = colId,
                kind = ComponentKind.Column,
                children = listOf(
                    DesignNode(id = childA, kind = ComponentKind.Button),
                    DesignNode(id = childB, kind = ComponentKind.Text),
                    DesignNode(id = childC, kind = ComponentKind.Image)
                )
            )

            store.execute(AddNodeCommand(screenId, screen.root.id, column))

            return Triple(store, screenId, listOf(childA, childB, childC))
        } catch (e: Throwable) {
            store.close()
            throw e
        }
    }

    @Test
    fun testMoveNodeWithinParentCommandRoundTripAllPermutations() {
        val (store, screenId, children) = setupStoreWithThreeChildren()
        try {
            val (a, b, c) = children

            fun currentChildIds(): List<NodeId> {
                val sc = store.project.value!!.screens.first { it.id == screenId }
                val col = sc.root.findNode(NodeId("col-1"))!!
                return col.children.map { it.id }
            }

            assertEquals(listOf(a, b, c), currentChildIds())

            // 1. Move A (index 0) -> index 1
            store.reorderNode(a, 1)
            assertEquals(listOf(b, a, c), currentChildIds())
            store.undo()
            assertEquals(listOf(a, b, c), currentChildIds())
            store.redo()
            assertEquals(listOf(b, a, c), currentChildIds())
            store.undo() // back to [A, B, C]

            // 2. Move A (index 0) -> index 2
            store.reorderNode(a, 2)
            assertEquals(listOf(b, c, a), currentChildIds())
            store.undo()
            assertEquals(listOf(a, b, c), currentChildIds())
            store.redo()
            assertEquals(listOf(b, c, a), currentChildIds())
            store.undo() // back to [A, B, C]

            // 3. Move B (index 1) -> index 0
            store.reorderNode(b, 0)
            assertEquals(listOf(b, a, c), currentChildIds())
            store.undo()
            assertEquals(listOf(a, b, c), currentChildIds())

            // 4. Move B (index 1) -> index 2
            store.reorderNode(b, 2)
            assertEquals(listOf(a, c, b), currentChildIds())
            store.undo()
            assertEquals(listOf(a, b, c), currentChildIds())

            // 5. Move C (index 2) -> index 0
            store.reorderNode(c, 0)
            assertEquals(listOf(c, a, b), currentChildIds())
            store.undo()
            assertEquals(listOf(a, b, c), currentChildIds())

            // 6. Move C (index 2) -> index 1
            store.reorderNode(c, 1)
            assertEquals(listOf(a, c, b), currentChildIds())
            store.undo()
            assertEquals(listOf(a, b, c), currentChildIds())
        } finally {
            store.close()
        }
    }

    @Test
    fun testIndicatorIndexComputationPureFunction() {
        val a = NodeId("A")
        val b = NodeId("B")
        val c = NodeId("C")

        val parentBounds = Rect(0f, 0f, 300f, 600f)
        val siblingsVertical = listOf(
            a to Rect(10f, 10f, 290f, 110f),  // mid = 60
            b to Rect(10f, 120f, 290f, 220f), // mid = 170
            c to Rect(10f, 230f, 290f, 330f)  // mid = 280
        )

        // Dragging A (index 0) in vertical Column
        // Pointer above mid A: slot 0 -> targetIndex 0, isValid = false
        val resA0 = DragReorderHelper.computeDropTarget(Offset(100f, 40f), a, parentBounds, siblingsVertical, isVertical = true)
        assertNotNull(resA0)
        assertEquals(0, resA0.targetIndex)
        assertFalse(resA0.isValid)

        // Pointer between mid A and mid B: slot 1 -> targetIndex 0, isValid = false
        val resA1 = DragReorderHelper.computeDropTarget(Offset(100f, 100f), a, parentBounds, siblingsVertical, isVertical = true)
        assertNotNull(resA1)
        assertEquals(0, resA1.targetIndex)
        assertFalse(resA1.isValid)

        // Pointer between mid B and mid C: slot 2 -> targetIndex 1, isValid = true (drop between B and C)
        val resA2 = DragReorderHelper.computeDropTarget(Offset(100f, 200f), a, parentBounds, siblingsVertical, isVertical = true)
        assertNotNull(resA2)
        assertEquals(1, resA2.targetIndex)
        assertTrue(resA2.isValid)
        assertEquals(10f, resA2.indicatorLineStart.x)
        assertEquals(290f, resA2.indicatorLineEnd.x)
        // Midpoint between B.bottom (220) and C.top (230) = 225
        assertEquals(225f, resA2.indicatorLineStart.y)

        // Pointer below mid C: slot 3 -> targetIndex 2, isValid = true (drop after C)
        val resA3 = DragReorderHelper.computeDropTarget(Offset(100f, 300f), a, parentBounds, siblingsVertical, isVertical = true)
        assertNotNull(resA3)
        assertEquals(2, resA3.targetIndex)
        assertTrue(resA3.isValid)
        assertEquals(330f, resA3.indicatorLineStart.y) // at bottom of C

        // Dragging C (index 2) in vertical Column
        // Pointer above mid A: slot 0 -> targetIndex 0, isValid = true (drop at top)
        val resC0 = DragReorderHelper.computeDropTarget(Offset(100f, 40f), c, parentBounds, siblingsVertical, isVertical = true)
        assertNotNull(resC0)
        assertEquals(0, resC0.targetIndex)
        assertTrue(resC0.isValid)
        assertEquals(10f, resC0.indicatorLineStart.y) // top of A

        // Pointer between mid A and mid B: slot 1 -> targetIndex 1, isValid = true (drop between A and B)
        val resC1 = DragReorderHelper.computeDropTarget(Offset(100f, 100f), c, parentBounds, siblingsVertical, isVertical = true)
        assertNotNull(resC1)
        assertEquals(1, resC1.targetIndex)
        assertTrue(resC1.isValid)
        // Midpoint between A.bottom (110) and B.top (120) = 115
        assertEquals(115f, resC1.indicatorLineStart.y)

        // Horizontal Row test
        val siblingsHorizontal = listOf(
            a to Rect(10f, 10f, 110f, 100f),  // mid = 60
            b to Rect(120f, 10f, 220f, 100f), // mid = 170
            c to Rect(230f, 10f, 330f, 100f)  // mid = 280
        )
        val resRow = DragReorderHelper.computeDropTarget(Offset(200f, 50f), a, parentBounds, siblingsHorizontal, isVertical = false)
        assertNotNull(resRow)
        assertEquals(1, resRow.targetIndex)
        assertTrue(resRow.isValid)
        // Vertical line at midpoint between B.right (220) and C.left (230) = 225
        assertEquals(225f, resRow.indicatorLineStart.x)
        assertEquals(10f, resRow.indicatorLineStart.y)
        assertEquals(100f, resRow.indicatorLineEnd.y)
    }

    @Test
    fun testConstraintPointerOutsideParentReturnsNull() {
        val a = NodeId("A")
        val b = NodeId("B")
        val parentBounds = Rect(0f, 0f, 300f, 600f)
        val siblings = listOf(
            a to Rect(10f, 10f, 290f, 110f),
            b to Rect(10f, 120f, 290f, 220f)
        )

        // Outside to right
        assertNull(DragReorderHelper.computeDropTarget(Offset(350f, 100f), a, parentBounds, siblings, isVertical = true))
        // Outside to bottom
        assertNull(DragReorderHelper.computeDropTarget(Offset(100f, 700f), a, parentBounds, siblings, isVertical = true))
        // Outside to left
        assertNull(DragReorderHelper.computeDropTarget(Offset(-10f, 100f), a, parentBounds, siblings, isVertical = true))
        // Outside to top
        assertNull(DragReorderHelper.computeDropTarget(Offset(100f, -50f), a, parentBounds, siblings, isVertical = true))
    }

    @Test
    fun testThresholdCalculationDisambiguation() {
        val dragThreshold = 4f
        val start = Offset(100f, 100f)

        // 3dp movement -> below threshold (click)
        val pos3dp = Offset(103f, 100f)
        val dist3 = (pos3dp - start).getDistance()
        assertTrue(dist3 < dragThreshold, "3dp movement must not exceed 4dp threshold")

        // 5dp movement -> exceeds threshold (drag)
        val pos5dp = Offset(105f, 100f)
        val dist5 = (pos5dp - start).getDistance()
        assertTrue(dist5 > dragThreshold, "5dp movement must exceed 4dp threshold")
    }

    @Test
    fun testEscapeMidDragCancelsWithoutCommand() {
        val (store, _, _) = setupStoreWithThreeChildren()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        try {
            var canvasDragCancelled = false
            var isCanvasDragging = true

            val actions = EditorShortcutActions(
                isCanvasDragging = { isCanvasDragging },
                onCancelCanvasDrag = {
                    canvasDragCancelled = true
                    isCanvasDragging = false
                }
            )

            val undoCountBefore = store.undoStack.value.size
            val escape = KeyEvent(Key.Escape, KeyEventType.KeyDown)
            assertTrue(handleEditorKeyEvent(escape, store, panels, viewport, actions))
            assertTrue(canvasDragCancelled)
            assertFalse(isCanvasDragging)
            assertEquals(undoCountBefore, store.undoStack.value.size, "No command should be executed on drag cancellation")
        } finally {
            store.close()
        }
    }

    @Test
    fun testKeyboardNudge() {
        val (store, screenId, children) = setupStoreWithThreeChildren()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        try {
            val (a, b, c) = children

            fun currentChildIds(): List<NodeId> {
                val sc = store.project.value!!.screens.first { it.id == screenId }
                val col = sc.root.findNode(NodeId("col-1"))!!
                return col.children.map { it.id }
            }

            store.selectNode(b) // B is at index 1

            // 1. Ctrl+Up nudges B to index 0: [B, A, C]
            val ctrlUp = KeyEvent(Key.DirectionUp, KeyEventType.KeyDown, isCtrlPressed = true)
            assertTrue(handleEditorKeyEvent(ctrlUp, store, panels, viewport))
            assertEquals(listOf(b, a, c), currentChildIds())

            // 2. Clamped at top: B is now at index 0, Ctrl+Up should be no-op
            val undoCountBefore = store.undoStack.value.size
            handleEditorKeyEvent(ctrlUp, store, panels, viewport)
            assertEquals(undoCountBefore, store.undoStack.value.size)
            assertEquals(listOf(b, a, c), currentChildIds())

            // 3. Ctrl+Down nudges B to index 1: [A, B, C]
            val ctrlDown = KeyEvent(Key.DirectionDown, KeyEventType.KeyDown, isCtrlPressed = true)
            assertTrue(handleEditorKeyEvent(ctrlDown, store, panels, viewport))
            assertEquals(listOf(a, b, c), currentChildIds())

            // 4. Ctrl+Right (horizontal alias) nudges B to index 2: [A, C, B]
            val ctrlRight = KeyEvent(Key.DirectionRight, KeyEventType.KeyDown, isCtrlPressed = true)
            assertTrue(handleEditorKeyEvent(ctrlRight, store, panels, viewport))
            assertEquals(listOf(a, c, b), currentChildIds())

            // 5. Clamped at end: B is at index 2, Ctrl+Down should be no-op
            val undoCountAtEnd = store.undoStack.value.size
            handleEditorKeyEvent(ctrlDown, store, panels, viewport)
            assertEquals(undoCountAtEnd, store.undoStack.value.size)
            assertEquals(listOf(a, c, b), currentChildIds())

            // 6. Suppressed when typing in text fields
            val textActions = EditorShortcutActions(isTextInputFocused = { true })
            val ctrlLeft = KeyEvent(Key.DirectionLeft, KeyEventType.KeyDown, isCtrlPressed = true)
            val handledInText = handleEditorKeyEvent(ctrlLeft, store, panels, viewport, textActions)
            assertFalse(handledInText, "Ctrl+Arrow must be suppressed when text input is focused")
            assertEquals(undoCountAtEnd, store.undoStack.value.size)
        } finally {
            store.close()
        }
    }
}
