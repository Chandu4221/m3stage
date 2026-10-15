package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.adapter.persistence.mapper.toDomain
import io.github.chandu4221.m3stage.adapter.persistence.mapper.toDto
import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.NavigationBarDefinition
import io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition
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

class SlotIntegrityTest {

    private class TestIdGen : IdGenerator {
        private var count = 0
        override fun nextProjectId(): ProjectId = ProjectId("p-${++count}")
        override fun nextScreenId(): ScreenId = ScreenId("s-${++count}")
        override fun nextNodeId(): NodeId = NodeId("n-${++count}")
    }

    private fun createStore(idGen: IdGenerator = TestIdGen()): EditorStore {
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

    private fun assertSubtreeSlotIntegrity(root: DesignNode) {
        val allNodes = root.flatten()
        val allIds = allNodes.map { it.id }
        assertEquals(allIds.toSet().size, allIds.size, "All NodeIds in subtree must be globally unique")

        for (node in allNodes) {
            for ((slotId, slotChildren) in node.slots) {
                for (child in slotChildren) {
                    assertEquals(
                        slotId,
                        node.findSlotForNode(child.id),
                        "Child ${child.id.value} (${child.kind.displayName}) must be recognized in slot ${slotId.value} of parent ${node.id.value} (${node.kind.displayName})"
                    )
                    assertTrue(
                        node.allDirectChildren.any { it.id == child.id },
                        "Child ${child.id.value} must be part of parent.allDirectChildren"
                    )
                    assertNotNull(
                        node.findNode(child.id),
                        "Parent ${node.id.value} must be able to recursively find child ${child.id.value}"
                    )
                    assertNotNull(
                        root.findNode(child.id),
                        "Root must be able to recursively find child ${child.id.value}"
                    )
                }
            }
        }
    }

    @Test
    fun testReIdSubtreePreservesSlotIntegrityForAllSlotBearingDefinitions() {
        val idGen = TestIdGen()
        val slotBearingDefs = ComponentCatalog.all.filter { it.slots.isNotEmpty() }
        assertTrue(slotBearingDefs.isNotEmpty(), "Catalog must have slot-bearing definitions")

        for (def in slotBearingDefs) {
            val originalNode = def.createDefaultSubtree(idGen)

            // Re-id subtree (used by duplicate and copy-paste)
            val reIdNode = reIdSubtree(originalNode, idGen)

            // Assert slot counts and keys are preserved
            assertEquals(
                originalNode.slots.keys,
                reIdNode.slots.keys,
                "Slots keys must match after reId for ${def.kind.displayName}"
            )
            for (slotId in originalNode.slots.keys) {
                val origCount = originalNode.slots[slotId]?.size ?: 0
                val reIdCount = reIdNode.slots[slotId]?.size ?: 0
                assertEquals(
                    origCount,
                    reIdCount,
                    "Slot child counts for ${slotId.value} must match after reId for ${def.kind.displayName}"
                )
            }

            // Assert integrity across the reId'd node
            assertSubtreeSlotIntegrity(reIdNode)
        }
    }

    @Test
    fun testDuplicateNodePreservesSlotIntegrityInEditorStore() {
        val idGen = TestIdGen()
        val duplicableSlotBearingKinds = listOf(
            ComponentKind.Button,
            ComponentKind.IconButton,
            ComponentKind.FloatingActionButton,
            ComponentKind.OutlinedButton,
            ComponentKind.TextButton,
            ComponentKind.Card,
            ComponentKind.NavigationBarItem
        )

        for (kind in duplicableSlotBearingKinds) {
            val store = createStore(idGen)
            try {
                val screenId = store.project.value!!.screens.first().id
                store.setActiveScreen(screenId)
                val screen = store.project.value!!.findScreen(screenId)!!

                val col = screen.root.allDirectChildren.first { it.kind == ComponentKind.Column }

                val targetNodeId = if (kind == ComponentKind.NavigationBarItem) {
                    val navBar = NavigationBarDefinition.definition.createDefaultSubtree(idGen)
                    store.execute(AddNodeCommand(screenId, screen.root.id, navBar, ScaffoldDefinition.BottomBarSlot.id))
                    val items = navBar.slots[NavigationBarDefinition.ItemsSlot.id] ?: emptyList()
                    assertTrue(items.isNotEmpty(), "NavigationBar must have default items")
                    items.first().id
                } else {
                    val def = ComponentCatalog.find(kind)!!
                    val newNode = def.createDefaultSubtree(idGen)
                    store.execute(AddNodeCommand(screenId, col.id, newNode, null))
                    newNode.id
                }

                val activeScreen = store.project.value!!.findScreen(screenId)!!
                assertSubtreeSlotIntegrity(activeScreen.root)

                // Duplicate the target node
                store.duplicateNode(targetNodeId)

                val afterDupScreen = store.project.value!!.findScreen(screenId)!!
                assertSubtreeSlotIntegrity(afterDupScreen.root)
            } finally {
                store.close()
            }
        }
    }

    @Test
    fun testCopyAndPastePreservesSlotIntegrityInEditorStore() {
        val idGen = TestIdGen()
        val pasteableSlotBearingKinds = listOf(
            ComponentKind.Button,
            ComponentKind.IconButton,
            ComponentKind.FloatingActionButton,
            ComponentKind.OutlinedButton,
            ComponentKind.TextButton,
            ComponentKind.Card,
            ComponentKind.NavigationBarItem
        )

        for (kind in pasteableSlotBearingKinds) {
            val store = createStore(idGen)
            try {
                val screenId = store.project.value!!.screens.first().id
                store.setActiveScreen(screenId)
                val screen = store.project.value!!.findScreen(screenId)!!

                val col = screen.root.allDirectChildren.first { it.kind == ComponentKind.Column }

                val (targetNodeId, targetParentId) = if (kind == ComponentKind.NavigationBarItem) {
                    val navBar = NavigationBarDefinition.definition.createDefaultSubtree(idGen)
                    store.execute(AddNodeCommand(screenId, screen.root.id, navBar, ScaffoldDefinition.BottomBarSlot.id))
                    val items = navBar.slots[NavigationBarDefinition.ItemsSlot.id] ?: emptyList()
                    assertTrue(items.isNotEmpty(), "NavigationBar must have default items")
                    items.first().id to navBar.id
                } else {
                    val def = ComponentCatalog.find(kind)!!
                    val newNode = def.createDefaultSubtree(idGen)
                    store.execute(AddNodeCommand(screenId, col.id, newNode, null))
                    newNode.id to col.id
                }

                // Copy node
                store.copyNode(targetNodeId)
                assertNotNull(store.clipboardDelegate.clip.value)

                // Select target parent and paste
                store.selectNode(targetParentId)
                store.pasteNode()

                val afterPasteScreen = store.project.value!!.findScreen(screenId)!!
                assertSubtreeSlotIntegrity(afterPasteScreen.root)
            } finally {
                store.close()
            }
        }
    }

    @Test
    fun testPersistenceRoundTripPreservesSlotIntegrity() {
        val idGen = TestIdGen()
        val slotBearingDefs = ComponentCatalog.all.filter {
            it.slots.isNotEmpty() &&
            it.kind != ComponentKind.NavigationBarItem &&
            it.kind != ComponentKind.NavigationBar &&
            it.kind != ComponentKind.Scaffold &&
            it.kind != ComponentKind.TopAppBar
        }

        // All general slot-bearing components can be children of Column
        val rootChildren = slotBearingDefs.map { def ->
            def.createDefaultSubtree(idGen)
        }

        val topBar = ComponentCatalog[ComponentKind.TopAppBar].createDefaultSubtree(idGen)
        val navBar = ComponentCatalog[ComponentKind.NavigationBar].createDefaultSubtree(idGen)
        val contentCol = DesignNode(
            id = NodeId("content-col"),
            kind = ComponentKind.Column,
            children = rootChildren
        )

        val scaffoldRoot = DesignNode(
            id = NodeId("scaffold-root"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(
                ScaffoldDefinition.TopBarSlot.id to listOf(topBar),
                ScaffoldDefinition.BottomBarSlot.id to listOf(navBar),
                ScaffoldDefinition.ContentSlot.id to listOf(contentCol)
            )
        )

        val screen = Screen(
            id = ScreenId("screen-1"),
            name = "TestScreen",
            route = "test_screen",
            root = scaffoldRoot
        )

        val project = Project(
            id = ProjectId("project-1"),
            name = "SlotIntegrityProject",
            basePackage = "com.example.app",
            screens = listOf(screen)
        )

        // Initial integrity
        assertSubtreeSlotIntegrity(screen.root)

        // Map to DTO and back to Domain
        val dto = project.toDto()
        val decodedProject = dto.toDomain()

        val decodedRoot = decodedProject.screens.first().root
        assertSubtreeSlotIntegrity(decodedRoot)

        assertEquals(project, decodedProject, "Project with all slot-bearing components must round-trip losslessly")
    }
}
