package io.github.chandu4221.m3stage.ui.page

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.*
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import io.github.chandu4221.m3stage.state.CanvasPointerTool
import io.github.chandu4221.m3stage.state.CanvasViewportDelegate
import io.github.chandu4221.m3stage.state.EditorStore
import io.github.chandu4221.m3stage.state.ShellPanelsController
import kotlin.test.*

@OptIn(InternalComposeUiApi::class)
class EditorKeyboardShortcutsTest {

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
        var saveCallCount = 0
        var lastSavedProject: Project? = null

        override suspend fun save(project: Project) {
            saveCallCount++
            lastSavedProject = project
        }

        override suspend fun load(): Project? = null
    }

    private val idGen = TestIdGen()
    private val repo = MockProjectRepo()
    private val codeGen = DummyCodeGen()

    private fun createStoreWithScreen(): Triple<EditorStore, ScreenId, NodeId> {
        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()
        val screen = store.project.value!!.screens.first()
        store.setActiveScreen(screen.id)
        return Triple(store, screen.id, screen.root.id)
    }

    @Test
    fun testPanelToggleShortcutsCtrlShift123() {
        val (store, _, _) = createStoreWithScreen()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        assertTrue(panels.isLeftOpen)
        assertTrue(panels.isRightOpen)
        assertFalse(panels.isBottomOpen)

        // Ctrl+Shift+1 toggles left panel
        val ctrlShift1 = KeyEvent(Key.One, KeyEventType.KeyDown, isCtrlPressed = true, isShiftPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlShift1, store, panels, viewport))
        assertFalse(panels.isLeftOpen)

        // Ctrl+Shift+2 toggles right panel
        val ctrlShift2 = KeyEvent(Key.Two, KeyEventType.KeyDown, isCtrlPressed = true, isShiftPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlShift2, store, panels, viewport))
        assertFalse(panels.isRightOpen)

        // Ctrl+Shift+3 toggles bottom panel
        val ctrlShift3 = KeyEvent(Key.Three, KeyEventType.KeyDown, isCtrlPressed = true, isShiftPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlShift3, store, panels, viewport))
        assertTrue(panels.isBottomOpen)
    }

    @Test
    fun testDuplicateAndSaveShortcuts() {
        val (store, _, rootId) = createStoreWithScreen()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        store.selectNode(rootId)

        // Ctrl+S saves project
        val ctrlS = KeyEvent(Key.S, KeyEventType.KeyDown, isCtrlPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlS, store, panels, viewport))

        // Ctrl+D duplicates selected node
        val ctrlD = KeyEvent(Key.D, KeyEventType.KeyDown, isCtrlPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlD, store, panels, viewport))
    }

    @Test
    fun testZoomShortcuts() {
        val (store, _, _) = createStoreWithScreen()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        val initialZoom = viewport.viewportState.value.zoom

        // Ctrl+= zooms in
        val ctrlPlus = KeyEvent(Key.Equals, KeyEventType.KeyDown, isCtrlPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlPlus, store, panels, viewport))
        assertTrue(viewport.viewportState.value.zoom > initialZoom)

        // Ctrl+- zooms out
        val ctrlMinus = KeyEvent(Key.Minus, KeyEventType.KeyDown, isCtrlPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlMinus, store, panels, viewport))

        // Ctrl+0 resets zoom
        val ctrlZero = KeyEvent(Key.Zero, KeyEventType.KeyDown, isCtrlPressed = true)
        assertTrue(handleEditorKeyEvent(ctrlZero, store, panels, viewport))
        assertEquals(1.0f, viewport.viewportState.value.zoom)
    }

    @Test
    fun testEscapeThreeLevelCascade() {
        val (store, screenId, rootId) = createStoreWithScreen()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        var paletteCancelled = false
        var isPaletteDragging = true
        val actions = EditorShortcutActions(
            isPaletteDragging = { isPaletteDragging },
            onCancelPaletteDrag = {
                paletteCancelled = true
                isPaletteDragging = false
            }
        )

        store.setActiveScreen(screenId)
        store.selectNode(rootId)

        val escape = KeyEvent(Key.Escape, KeyEventType.KeyDown)

        // Level 1: Palette drag cancellation
        assertTrue(handleEditorKeyEvent(escape, store, panels, viewport, actions))
        assertTrue(paletteCancelled)
        assertEquals(rootId, store.selectedNodeId.value)
        assertEquals(screenId, store.activeScreenId.value)

        // Level 2: Node selection clearing
        assertTrue(handleEditorKeyEvent(escape, store, panels, viewport, actions))
        assertNull(store.selectedNodeId.value)
        assertEquals(screenId, store.activeScreenId.value)

        // Level 3: Screen selection clearing
        assertTrue(handleEditorKeyEvent(escape, store, panels, viewport, actions))
        assertNull(store.activeScreenId.value)

        // Further escape does nothing
        assertFalse(handleEditorKeyEvent(escape, store, panels, viewport, actions))
    }

    @Test
    fun testDeleteSuppressedWhenTextInputFocused() {
        val (store, _, rootId) = createStoreWithScreen()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        store.selectNode(rootId)

        var textInputFocused = true
        val actions = EditorShortcutActions(isTextInputFocused = { textInputFocused })

        val deleteKey = KeyEvent(Key.Delete, KeyEventType.KeyDown)
        val backspaceKey = KeyEvent(Key.Backspace, KeyEventType.KeyDown)

        // When focused in text field, delete and backspace are suppressed
        assertFalse(handleEditorKeyEvent(deleteKey, store, panels, viewport, actions))
        assertEquals(rootId, store.selectedNodeId.value)

        assertFalse(handleEditorKeyEvent(backspaceKey, store, panels, viewport, actions))
        assertEquals(rootId, store.selectedNodeId.value)

        // When text field is not focused, delete/backspace executes
        textInputFocused = false
        assertTrue(handleEditorKeyEvent(deleteKey, store, panels, viewport, actions))
    }

    @Test
    fun testToolShortcutsVAndHWithFocusSuppression() {
        val (store, _, _) = createStoreWithScreen()
        val panels = ShellPanelsController()
        val viewport = CanvasViewportDelegate()

        viewport.setActiveTool(CanvasPointerTool.Select)

        var textInputFocused = true
        val actions = EditorShortcutActions(isTextInputFocused = { textInputFocused })

        val keyH = KeyEvent(Key.H, KeyEventType.KeyDown)
        val keyV = KeyEvent(Key.V, KeyEventType.KeyDown)

        // When typing in text input, V and H are suppressed
        assertFalse(handleEditorKeyEvent(keyH, store, panels, viewport, actions))
        assertEquals(CanvasPointerTool.Select, viewport.viewportState.value.activeTool)

        // When not in text input, V and H switch active pointer tool
        textInputFocused = false
        assertTrue(handleEditorKeyEvent(keyH, store, panels, viewport, actions))
        assertEquals(CanvasPointerTool.Pan, viewport.viewportState.value.activeTool)

        assertTrue(handleEditorKeyEvent(keyV, store, panels, viewport, actions))
        assertEquals(CanvasPointerTool.Select, viewport.viewportState.value.activeTool)
    }
}
