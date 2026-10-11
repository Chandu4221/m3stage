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
        assertTrue(buttonNode.children.isNotEmpty(), "Button should have default label child")

        val labelNode = buttonNode.children.first()
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
}