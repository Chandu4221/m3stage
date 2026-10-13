package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScreenCommandTest {

    private class TestIdGen : IdGenerator {
        private var count = 0
        override fun nextProjectId(): ProjectId = ProjectId("p-${++count}")
        override fun nextScreenId(): ScreenId = ScreenId("s-${++count}")
        override fun nextNodeId(): NodeId = NodeId("n-${++count}")
    }

    private val idGen = TestIdGen()

    private fun createSampleProject(): Project {
        val root1 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val root2 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        return Project(
            id = idGen.nextProjectId(),
            name = "TestProject",
            basePackage = "com.example.test",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Home", route = "/", root = root1),
                Screen(id = ScreenId("screen-2"), name = "Profile", route = "/profile", root = root2)
            )
        )
    }

    @Test
    fun testRenameScreenCommand() {
        val project = createSampleProject()
        val command = RenameScreenCommand(ScreenId("screen-1"), "Home", "Dashboard")

        val executed = command.execute(project)
        assertEquals("Dashboard", executed.findScreen(ScreenId("screen-1"))?.name)
        assertTrue(ProjectValidation.validate(executed).isEmpty())

        val undone = command.undo(executed)
        assertEquals("Home", undone.findScreen(ScreenId("screen-1"))?.name)
        assertTrue(ProjectValidation.validate(undone).isEmpty())
    }

    @Test
    fun testDeleteScreenCommand() {
        val project = createSampleProject()
        val screenToDelete = project.screens[0]
        val command = DeleteScreenCommand(screenToDelete, 0)

        val executed = command.execute(project)
        assertEquals(1, executed.screens.size)
        assertEquals(ScreenId("screen-2"), executed.screens[0].id)
        assertTrue(ProjectValidation.validate(executed).isEmpty())

        val undone = command.undo(executed)
        assertEquals(2, undone.screens.size)
        assertEquals(ScreenId("screen-1"), undone.screens[0].id)
        assertTrue(ProjectValidation.validate(undone).isEmpty())
    }

    @Test
    fun testDuplicateScreenCommand() {
        val project = createSampleProject()
        val duplicatedScreen = Screen(
            id = ScreenId("screen-dup"),
            name = "Home_Copy",
            route = "/home-copy",
            root = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        )

        val command = DuplicateScreenCommand(duplicatedScreen)
        val executed = command.execute(project)
        assertEquals(3, executed.screens.size)
        assertEquals("Home_Copy", executed.screens.last().name)
        assertTrue(ProjectValidation.validate(executed).isEmpty())

        val undone = command.undo(executed)
        assertEquals(2, undone.screens.size)
        assertTrue(ProjectValidation.validate(undone).isEmpty())
    }

    @Test
    fun testReorderScreensCommand() {
        val project = createSampleProject()
        val command = ReorderScreensCommand(0, 1)

        val executed = command.execute(project)
        assertEquals(ScreenId("screen-2"), executed.screens[0].id)
        assertEquals(ScreenId("screen-1"), executed.screens[1].id)
        assertTrue(ProjectValidation.validate(executed).isEmpty())

        val undone = command.undo(executed)
        assertEquals(ScreenId("screen-1"), undone.screens[0].id)
        assertEquals(ScreenId("screen-2"), undone.screens[1].id)
        assertTrue(ProjectValidation.validate(undone).isEmpty())
    }

    @Test
    fun testMoveScreenCommand() {
        val project = createSampleProject()
        val command = MoveScreenCommand(ScreenId("screen-1"), 200f, 150f)

        val executed = command.execute(project)
        val screen = executed.findScreen(ScreenId("screen-1"))
        assertEquals(200f, screen?.canvasX)
        assertEquals(150f, screen?.canvasY)

        val undone = command.undo(executed)
        val undoneScreen = undone.findScreen(ScreenId("screen-1"))
        assertEquals(0f, undoneScreen?.canvasX)
        assertEquals(0f, undoneScreen?.canvasY)
    }

    @Test
    fun testTidyScreensCommand() {
        val project = createSampleProject()
        val newPositions = mapOf(
            ScreenId("screen-1") to (0f to 0f),
            ScreenId("screen-2") to (476f to 0f)
        )
        val command = TidyScreensCommand(newPositions)

        val executed = command.execute(project)
        assertEquals(0f, executed.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(476f, executed.findScreen(ScreenId("screen-2"))?.canvasX)

        val undone = command.undo(executed)
        assertEquals(0f, undone.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(0f, undone.findScreen(ScreenId("screen-2"))?.canvasX)
    }
}
