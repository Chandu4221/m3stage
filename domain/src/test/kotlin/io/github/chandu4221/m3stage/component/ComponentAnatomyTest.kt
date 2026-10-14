package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.component.slot.SlotId
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.port.IdGenerator
import kotlin.test.*

class ComponentAnatomyTest {

    private class TestIdGenerator : IdGenerator {
        private var counter = 1
        override fun nextNodeId(): NodeId = NodeId("test_node_${counter++}")
        override fun nextScreenId() = throw UnsupportedOperationException()
        override fun nextProjectId() = throw UnsupportedOperationException()
    }

    private val idGen = TestIdGenerator()

    @Test
    fun testButtonDefaultAnatomyContainsTextChild() {
        val buttonDef = ComponentCatalog[ComponentKind.Button]
        val buttonNode = buttonDef.createDefaultSubtree(idGen)

        assertEquals(ComponentKind.Button, buttonNode.kind)
        assertTrue(buttonNode.allDirectChildren.isNotEmpty(), "Button should have default label child")

        val labelNode = buttonNode.allDirectChildren.first()
        assertEquals(ComponentKind.Text, labelNode.kind)
        assertEquals("Button", labelNode[ComponentCatalog.TextProps.TextContent]?.value)

        // Slot verification
        val slotChildren = buttonNode.slots[SlotId("label")]
        assertNotNull(slotChildren, "Button should populate label slot")
        assertEquals(1, slotChildren.size)
        assertEquals(ComponentKind.Text, slotChildren.first().kind)
    }

    @Test
    fun testTopAppBarDefaultAnatomyContainsTitle() {
        val topAppBarDef = ComponentCatalog[ComponentKind.TopAppBar]
        val topAppBarNode = topAppBarDef.createDefaultSubtree(idGen)

        assertEquals(ComponentKind.TopAppBar, topAppBarNode.kind)
        val titleSlotChildren = topAppBarNode.slots[SlotId("title")]
        assertNotNull(titleSlotChildren)
        assertEquals(1, titleSlotChildren.size)
        assertEquals(ComponentKind.Text, titleSlotChildren.first().kind)
        assertEquals("Title", titleSlotChildren.first()[ComponentCatalog.TextProps.TextContent]?.value)
    }

    @Test
    fun testScaffoldDefaultAnatomyContainsTopBarAndContent() {
        val scaffoldDef = ComponentCatalog[ComponentKind.Scaffold]
        val scaffoldNode = scaffoldDef.createDefaultSubtree(idGen)

        assertEquals(ComponentKind.Scaffold, scaffoldNode.kind)

        val topBarSlot = scaffoldNode.slots[SlotId("topBar")]
        assertNotNull(topBarSlot, "Scaffold must populate topBar slot")
        assertEquals(ComponentKind.TopAppBar, topBarSlot.first().kind)

        val contentSlot = scaffoldNode.slots[SlotId("content")]
        assertNotNull(contentSlot, "Scaffold must populate content slot")
        assertEquals(ComponentKind.Column, contentSlot.first().kind)
    }

    @Test
    fun testCanAcceptChildEnforcesAtomicConstraints() {
        val buttonDef = ComponentCatalog[ComponentKind.Button]

        // Button accepts Text and Icon
        assertTrue(buttonDef.canAcceptChild(ComponentKind.Text))
        assertTrue(buttonDef.canAcceptChild(ComponentKind.Icon))

        // Button rejects Scaffold, Column, TextField
        assertFalse(buttonDef.canAcceptChild(ComponentKind.Scaffold))
        assertFalse(buttonDef.canAcceptChild(ComponentKind.Column))
        assertFalse(buttonDef.canAcceptChild(ComponentKind.TextField))
    }

    @Test
    fun testCanAcceptChildSlotSpecificValidation() {
        val topAppBarDef = ComponentCatalog[ComponentKind.TopAppBar]

        // title slot only accepts Text
        assertTrue(topAppBarDef.canAcceptChild(ComponentKind.Text, SlotId("title")))
        assertFalse(topAppBarDef.canAcceptChild(ComponentKind.Button, SlotId("title")))

        // navigationIcon slot accepts Icon or Button
        assertTrue(topAppBarDef.canAcceptChild(ComponentKind.Icon, SlotId("navigationIcon")))
        assertTrue(topAppBarDef.canAcceptChild(ComponentKind.Button, SlotId("navigationIcon")))
        assertFalse(topAppBarDef.canAcceptChild(ComponentKind.Column, SlotId("navigationIcon")))
    }

    @Test
    fun testNavigationBarDefaultAnatomyContainsFourItems() {
        val navBarDef = ComponentCatalog[ComponentKind.NavigationBar]
        val navBarNode = navBarDef.createDefaultSubtree(idGen)

        assertEquals(ComponentKind.NavigationBar, navBarNode.kind)
        val itemsSlot = navBarNode.slots[SlotId("items")]
        assertNotNull(itemsSlot, "NavigationBar must populate items slot")
        assertEquals(4, itemsSlot.size)

        val expectedLabels = listOf("Home", "Search", "Settings", "Profile")
        itemsSlot.forEachIndexed { index, itemNode ->
            assertEquals(ComponentKind.NavigationBarItem, itemNode.kind)
            val isSelected = itemNode[ComponentCatalog.NavigationBarItemProps.Selected]?.value ?: false
            if (index == 0) {
                assertTrue(isSelected, "First item ('Home') must be selected")
            } else {
                assertFalse(isSelected, "Item at index $index must not be selected by default")
            }

            val iconSlot = itemNode.slots[SlotId("icon")]
            assertNotNull(iconSlot, "Item must populate icon slot")
            assertEquals(1, iconSlot.size)
            assertEquals(ComponentKind.Icon, iconSlot.first().kind)

            val labelSlot = itemNode.slots[SlotId("label")]
            assertNotNull(labelSlot, "Item must populate label slot")
            assertEquals(1, labelSlot.size)
            assertEquals(ComponentKind.Text, labelSlot.first().kind)
            assertEquals(expectedLabels[index], labelSlot.first()[ComponentCatalog.TextProps.TextContent]?.value)
        }
    }

    @Test
    fun testAllCatalogDefinitionsFillRequiredSlotsAndComplyWithAllowedKinds() {
        for (def in ComponentCatalog.all) {
            val rootNode = def.createDefaultSubtree(idGen)

            // Validate top-level slots
            for (slot in def.slots) {
                val slotChildren = rootNode.slots[slot.id]
                if (slot.isRequired) {
                    assertNotNull(
                        slotChildren,
                        "Component ${def.kind} declares required slot '${slot.id.value}' but it was null"
                    )
                    assertTrue(
                        slotChildren.isNotEmpty(),
                        "Component ${def.kind} declares required slot '${slot.id.value}' but it was empty"
                    )
                }

                slotChildren?.forEach { child ->
                    val allowed = slot.allowedKinds
                    if (allowed != null) {
                        assertTrue(
                            child.kind in allowed,
                            "Slot '${slot.id.value}' in ${def.kind} contains child ${child.kind} not in allowed: $allowed"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun testAllDefaultSubtreesHaveUniqueNodeIds() {
        for (def in ComponentCatalog.all) {
            val rootNode = def.createDefaultSubtree(idGen)
            val seenIds = mutableSetOf<NodeId>()

            fun collectIds(node: io.github.chandu4221.m3stage.model.DesignNode) {
                val added = seenIds.add(node.id)
                assertTrue(
                    added,
                    "Duplicate nodeId ${node.id.value} detected in default subtree of ${def.kind}"
                )
                node.allDirectChildren.forEach { collectIds(it) }
            }

            collectIds(rootNode)
        }
    }
}