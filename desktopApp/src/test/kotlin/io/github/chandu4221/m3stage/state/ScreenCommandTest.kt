package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.slot.SlotId
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
        val oldPositions = project.screens.associate { it.id to (it.canvasX to it.canvasY) }
        val newPositions = mapOf(
            ScreenId("screen-1") to (0f to 0f),
            ScreenId("screen-2") to (476f to 0f)
        )
        val command = TidyScreensCommand(oldPositions, newPositions)

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
        val move1 = MoveScreenCommand(ScreenId("screen-1"), 0f, 0f, 300f, 150f)
        currentProject = move1.execute(currentProject)
        val move2 = MoveScreenCommand(ScreenId("screen-2"), 0f, 0f, 700f, 400f)
        currentProject = move2.execute(currentProject)

        assertEquals(300f, currentProject.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(150f, currentProject.findScreen(ScreenId("screen-1"))?.canvasY)
        assertEquals(700f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(400f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)

        // 2. Tidy screens in a row
        val oldTidy = currentProject.screens.associate { it.id to (it.canvasX to it.canvasY) }
        val tidyMap = mapOf(
            ScreenId("screen-1") to (0f to 0f),
            ScreenId("screen-2") to (476f to 0f)
        )
        val tidy = TidyScreensCommand(oldTidy, tidyMap)
        currentProject = tidy.execute(currentProject)

        assertEquals(0f, currentProject.findScreen(ScreenId("screen-1"))?.canvasX)
        assertEquals(0f, currentProject.findScreen(ScreenId("screen-1"))?.canvasY)
        assertEquals(476f, currentProject.findScreen(ScreenId("screen-2"))?.canvasX)
        assertEquals(0f, currentProject.findScreen(ScreenId("screen-2"))?.canvasY)

        // 3. Move Screen 2 again from its tidy anchor (+100f, +50f)
        val move3 = MoveScreenCommand(ScreenId("screen-2"), 476f, 0f, 476f + 100f, 0f + 50f)
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

        val oldPositions = project.screens.associate { it.id to (it.canvasX to it.canvasY) }
        val tidy = TidyScreensCommand(oldPositions, newPositions)
        val tidiedProject = tidy.execute(project)

        assertEquals(0f, tidiedProject.findScreen(ScreenId("screen-1"))?.canvasX)
        // Screen 2 starts after s1 width (412) + 64 = 476
        assertEquals(476f, tidiedProject.findScreen(ScreenId("screen-2"))?.canvasX)
        // Screen 3 starts after s2 width in landscape (892) + 64 = 476 + 892 + 64 = 1432
        assertEquals(1432f, tidiedProject.findScreen(ScreenId("screen-3"))?.canvasX)
    }

    @Test
    fun testEditorStoreTidyScreensRespectsScreenOrientationWidth() {
        val repo = object : io.github.chandu4221.m3stage.port.ProjectRepository {
            override suspend fun save(project: Project) {}
            override suspend fun load(): Project? = null
        }
        val codeGen = object : io.github.chandu4221.m3stage.port.CodeGenerator {
            override fun generate(project: Project): io.github.chandu4221.m3stage.port.GeneratedProject =
                io.github.chandu4221.m3stage.port.GeneratedProject(emptyList())
        }

        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()

        val initialScreen = store.project.value!!.screens.first()
        store.duplicateScreen(initialScreen.id)

        val screen1 = store.project.value!!.screens[0]
        val screen2 = store.project.value!!.screens[1]

        // Toggle screen1 from Portrait to Landscape (Pixel 8 landscape width = 892)
        store.toggleScreenOrientation(screen1.id)
        assertEquals(DeviceOrientation.Landscape, store.project.value!!.screens[0].orientation)

        // Tidy screens
        store.tidyScreens()

        val tidied = store.project.value!!
        assertEquals(0f, tidied.findScreen(screen1.id)?.canvasX)
        // Screen 2 starts after s1 landscape width (892) + 64 = 956f (not portrait 412 + 64 = 476f)
        assertEquals(956f, tidied.findScreen(screen2.id)?.canvasX)
    }

    @Test
    fun testRemoveNodeCommandSlotAwareAndIndexPreservingUndo() {
        val btn1 = DesignNode(id = NodeId("btn-1"), kind = ComponentKind.Button)
        val btn2 = DesignNode(id = NodeId("btn-2"), kind = ComponentKind.Button)
        val btn3 = DesignNode(id = NodeId("btn-3"), kind = ComponentKind.Button)
        val topBar = DesignNode(id = NodeId("top-bar"), kind = ComponentKind.TopAppBar)
        val scaffold = DesignNode(
            id = NodeId("scaffold-1"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(SlotId("topBar") to listOf(topBar)),
            children = listOf(btn1, btn2, btn3)
        )
        val project = Project(
            id = idGen.nextProjectId(),
            name = "Test",
            basePackage = "com.test",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Main", route = "/", root = scaffold)
            )
        )

        // 1. Remove middle child (btn-2 at index 1)
        val removeMiddle = RemoveNodeCommand(ScreenId("screen-1"), NodeId("btn-2"))
        val projectAfterRemoveChild = removeMiddle.execute(project)
        val screenAfterRemoveChild = projectAfterRemoveChild.findScreen(ScreenId("screen-1"))!!
        assertEquals(listOf(NodeId("btn-1"), NodeId("btn-3")), screenAfterRemoveChild.root.children.map { it.id })

        val restoredAfterRemoveChild = removeMiddle.undo(projectAfterRemoveChild)
        val screenRestoredChild = restoredAfterRemoveChild.findScreen(ScreenId("screen-1"))!!
        assertEquals(listOf(NodeId("btn-1"), NodeId("btn-2"), NodeId("btn-3")), screenRestoredChild.root.children.map { it.id })

        // 2. Remove node from a slot (topBar from slot 'topBar')
        val removeSlotNode = RemoveNodeCommand(ScreenId("screen-1"), NodeId("top-bar"))
        val projectAfterRemoveSlot = removeSlotNode.execute(project)
        val screenAfterRemoveSlot = projectAfterRemoveSlot.findScreen(ScreenId("screen-1"))!!
        assertTrue(screenAfterRemoveSlot.root.slots[SlotId("topBar")].isNullOrEmpty())

        val restoredAfterRemoveSlot = removeSlotNode.undo(projectAfterRemoveSlot)
        val screenRestoredSlot = restoredAfterRemoveSlot.findScreen(ScreenId("screen-1"))!!
        assertEquals(listOf(NodeId("top-bar")), screenRestoredSlot.root.slots[SlotId("topBar")]?.map { it.id })
    }

    @Test
    fun testReorderScreensUndoPreservesIdentityAcrossDeletions() {
        val root1 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val root2 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val root3 = ComponentCatalog[ComponentKind.Scaffold].createDefaultSubtree(idGen)
        val s1 = Screen(id = ScreenId("s-1"), name = "S1", route = "/1", root = root1)
        val s2 = Screen(id = ScreenId("s-2"), name = "S2", route = "/2", root = root2)
        val s3 = Screen(id = ScreenId("s-3"), name = "S3", route = "/3", root = root3)

        val project = Project(
            id = idGen.nextProjectId(),
            name = "Test",
            basePackage = "com.test",
            screens = listOf(s1, s2, s3)
        )

        // Reorder: Move s-1 from index 0 to index 2 -> [s-2, s-3, s-1]
        val reorder = ReorderScreensCommand(0, 2)
        val reordered = reorder.execute(project)
        assertEquals(listOf(ScreenId("s-2"), ScreenId("s-3"), ScreenId("s-1")), reordered.screens.map { it.id })

        // Delete s-2 at index 0 -> [s-3, s-1]
        val delete = DeleteScreenCommand(s2, 0)
        val deleted = delete.execute(reordered)
        assertEquals(listOf(ScreenId("s-3"), ScreenId("s-1")), deleted.screens.map { it.id })

        // Undo delete -> restores s-2 at index 0 -> [s-2, s-3, s-1]
        val undoDeleted = delete.undo(deleted)
        assertEquals(listOf(ScreenId("s-2"), ScreenId("s-3"), ScreenId("s-1")), undoDeleted.screens.map { it.id })

        // Undo reorder -> restores s-1 back to index 0 by ID -> [s-1, s-2, s-3]
        val undoReordered = reorder.undo(undoDeleted)
        assertEquals(listOf(ScreenId("s-1"), ScreenId("s-2"), ScreenId("s-3")), undoReordered.screens.map { it.id })

        // Direct undo test: reorder again, delete s-2, then undo reorder without undoing delete first
        val reorderedAgain = reorder.execute(project)
        val deletedAgain = delete.execute(reorderedAgain) // [s-3, s-1]
        val directUndoReorder = reorder.undo(deletedAgain) // s-1 should be moved back to index 0 -> [s-1, s-3]
        assertEquals(listOf(ScreenId("s-1"), ScreenId("s-3")), directUndoReorder.screens.map { it.id })
    }

    @Test
    fun testMoveScreenCommandEqualityAndCopyPreservesUndoState() {
        val cmd = MoveScreenCommand(ScreenId("screen-1"), 10f, 20f, 100f, 200f)
        val copied = cmd.copy()
        assertEquals(cmd, copied)
        assertEquals(10f, copied.oldX)
        assertEquals(20f, copied.oldY)
        assertEquals(100f, copied.newX)
        assertEquals(200f, copied.newY)
    }

    @Test
    fun testTidyScreensCommandEqualityAndCopyPreservesUndoState() {
        val oldPos = mapOf(ScreenId("screen-1") to (10f to 20f))
        val newPos = mapOf(ScreenId("screen-1") to (100f to 200f))
        val cmd = TidyScreensCommand(oldPos, newPos)
        val copied = cmd.copy()
        assertEquals(cmd, copied)
        assertEquals(oldPos, copied.oldPositions)
        assertEquals(newPos, copied.newPositions)
    }

    @Test
    fun testDeselectScreenAndPreserveDeselectionAcrossMoves() {
        val repo = object : io.github.chandu4221.m3stage.port.ProjectRepository {
            override suspend fun save(project: Project) {}
            override suspend fun load(): Project? = null
        }
        val codeGen = object : io.github.chandu4221.m3stage.port.CodeGenerator {
            override fun generate(project: Project): io.github.chandu4221.m3stage.port.GeneratedProject =
                io.github.chandu4221.m3stage.port.GeneratedProject(emptyList())
        }

        val store = EditorStore(idGen, repo, codeGen)
        store.createNewProject()

        val screenId = store.project.value!!.screens.first().id
        store.setActiveScreen(screenId)
        assertEquals(screenId, store.activeScreenId.value)

        // Move the screen
        store.moveScreen(screenId, 150f, 100f)
        assertEquals(150f, store.project.value!!.findScreen(screenId)?.canvasX)

        // Click parent canvas to deselect screen
        store.setActiveScreen(null)
        kotlin.test.assertNull(store.activeScreenId.value)
        kotlin.test.assertNull(store.selectedNodeId.value)

        // Move screen again; selection should remain null (no re-select race)
        store.moveScreen(screenId, 200f, 120f)
        assertEquals(200f, store.project.value!!.findScreen(screenId)?.canvasX)
        kotlin.test.assertNull(store.activeScreenId.value)
    }
}

