package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.GeneratedProject
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveLayoutTest {

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
    fun testToggleScreenAdaptiveRoundTripAndUndoRedo() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            val initialScreen = store.project.value!!.screens.first { it.id == screenId }

            // Initially false
            assertFalse(initialScreen.adaptive)

            // Enable adaptive
            store.setScreenAdaptive(screenId, true)
            val updatedScreen = store.project.value!!.screens.first { it.id == screenId }
            assertTrue(updatedScreen.adaptive)

            // Undo restores false
            store.undo()
            val undoneScreen = store.project.value!!.screens.first { it.id == screenId }
            assertFalse(undoneScreen.adaptive)

            // Redo restores true
            store.redo()
            val redoneScreen = store.project.value!!.screens.first { it.id == screenId }
            assertTrue(redoneScreen.adaptive)
        } finally {
            store.close()
        }
    }

    @Test
    fun testWindowSizeClassRecomputesOnOrientationFlip() {
        val store = createStore()
        try {
            val screenId = store.project.value!!.screens.first().id
            val defaultDevice = store.project.value!!.defaultDevice // Pixel 8: 412x892

            val screen = store.project.value!!.screens.first { it.id == screenId }

            // Portrait (412dp) -> Compact
            assertEquals(DeviceOrientation.Portrait, screen.orientation)
            val portraitWidth = screen.resolveWidthDp(defaultDevice)
            assertEquals(412, portraitWidth)
            assertEquals(WindowSizeClass.Compact, screen.effectiveWindowSizeClass(portraitWidth))

            // Flip orientation to Landscape (892dp) -> Expanded
            store.toggleScreenOrientation(screenId)
            val landscapeScreen = store.project.value!!.screens.first { it.id == screenId }
            assertEquals(DeviceOrientation.Landscape, landscapeScreen.orientation)
            val landscapeWidth = landscapeScreen.resolveWidthDp(defaultDevice)
            assertEquals(892, landscapeWidth)
            assertEquals(WindowSizeClass.Expanded, landscapeScreen.effectiveWindowSizeClass(landscapeWidth))

            // Undo flip -> Portrait -> Compact
            store.undo()
            val revertedScreen = store.project.value!!.screens.first { it.id == screenId }
            assertEquals(DeviceOrientation.Portrait, revertedScreen.orientation)
            val revertedWidth = revertedScreen.resolveWidthDp(defaultDevice)
            assertEquals(412, revertedWidth)
            assertEquals(WindowSizeClass.Compact, revertedScreen.effectiveWindowSizeClass(revertedWidth))
        } finally {
            store.close()
        }
    }
}
