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

    @Test
    fun testMoveScreenThenTidyThenMoveAgainSequence() {
        var currentProject = createSampleProject()

        // 1. Move both screens to random positions
        val move1 = MoveScreenCommand(ScreenId("screen-1"), 300f, 150f)
        currentProject = move1.execute(currentProject)
        val move2 = MoveScreenCommand(ScreenId("screen-2"), 700f, 400f)
        currentProject = move2.execute(currentProject)

        assertEquals(300f, currentProject.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(150f, currentProject.findScreen(ScreenId("screen-1"))?.canvasY)
        assertEquals(700f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(400f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)

        // 2. Tidy screens in a row
        val tidyMap = mapOf(
            ScreenId("screen-1") to (0f to 0f),
            ScreenId("screen-2") to (476f to 0f)
        )
        val tidy = TidyScreensCommand(tidyMap)
        currentProject = tidy.execute(currentProject)

        assertEquals(0f, currentProject.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(0f, currentProject.findScreen(ScreenId("screen-1"))?.canvasY)
        assertEquals(476f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(0f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)

        // 3. Move Screen 2 again from its tidy anchor (+100f, +50f)
        val move3 = MoveScreenCommand(ScreenId("screen-2"), 476f + 100f, 0f + 50f)
        currentProject = move3.execute(currentProject)

        assertEquals(576f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(50f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)

        // 4. Undo move3 -> restores Screen 2 to tidy position
        currentProject = move3.undo(currentProject)
        assertEquals(476f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(0f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)

        // 5. Undo tidy -> restores both screens to their pre-tidy moved positions
        currentProject = tidy.undo(currentProject)
        assertEquals(300f, currentProject.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(150f, currentProject.findScreen(ScreenId("screen-1"))?.canvasY)
        assertEquals(700f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(400f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)
    }

    @Test
    fun testUpdateScreenMetaCommand() {
        val project = createSampleProject()
        val command = UpdateScreenMetaCommand(
            screenId = ScreenId("screen-1"),
            oldDevice = null,
            oldOrientation = DeviceOrientation.Portrait,
            newDevice = DevicePreset.GalaxyS24,
            newOrientation = DeviceOrientation.Landscape
        )

        val executed = command.execute(project)
        val screen = executed.findScreen(ScreenId("screen-1"))!!
        assertEquals(DevicePreset.GalaxyS24, screen.device)
        assertEquals(DeviceOrientation.Landscape, screen.orientation)

        val undone = command.undo(executed)
        val undoneScreen = undone.findScreen(ScreenId("screen-1"))!!
        assertEquals(null, undoneScreen.device)
        assertEquals(DeviceOrientation.Portrait, undoneScreen.orientation)
    }

    @Test
    fun testResolveWidthAndTidySpacingWithMixedDevicesAndOrientations() {
        val root1 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val root2 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val root3 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)

        val s1 = Screen(
            id = ScreenId("screen-1"),
            name = "PhonePortrait",
            route = "/s1",
            device = DevicePreset.Pixel8, // 412 x 892
            orientation = DeviceOrientation.Portrait,
            root = root1
        )
        val s2 = Screen(
            id = ScreenId("screen-2"),
            name = "PhoneLandscape",
            route = "/s2",
            device = DevicePreset.Pixel8, // 412 x 892 in Landscape -> width = 892
            orientation = DeviceOrientation.Landscape,
            root = root2
        )
        val s3 = Screen(
            id = ScreenId("screen-3"),
            name = "TabletPortrait",
            route = "/s3",
            device = DevicePreset.PixelTablet, // 1280 x 800 in Portrait -> width = 1280
            orientation = DeviceOrientation.Portrait,
            root = root3
        )

        assertEquals(412, s1.resolveWidthDp(DevicePreset.Default))
        assertEquals(892, s2.resolveWidthDp(DevicePreset.Default))
        assertEquals(1280, s3.resolveWidthDp(DevicePreset.Default))

        val project = Project(
            id = idGen.nextProjectId(),
            name = "MixedProject",
            basePackage = "com.example.test",
            defaultDevice = DevicePreset.Pixel8,
            screens = listOf(s1, s2, s3)
        )

        // Simulate tidy positions computation
        val newPositions = mutableMapOf<ScreenId, Pair<Float, Float>>()
        var currentX = 0f
        for (screen in project.screens) {
            newPositions[screen.id] = currentX to 0f
            currentX += screen.resolveWidthDp(project.defaultDevice) + 64f
        }

        val tidy = TidyScreensCommand(newPositions)
        val tidiedProject = tidy.execute(project)

        assertEquals(0f, tidiedProject.findScreen(ScreenId("screen-1"))?.canvasX)
        // Screen 2 starts after s1 width (412) + 64 = 476
        assertEquals(476f, tidiedProject.findScreen(ScreenId("screen-2"))?.canvasX)
        // Screen 3 starts after s2 width in landscape (892) + 64 = 476 + 892 + 64 = 1432
        assertEquals(1432f, tidiedProject.findScreen(ScreenId("screen-3"))?.canvasX)
    }
}

