package io.github.chandu4221.m3stage.query

import io.github.chandu4221.m3stage.component.ComponentCatalog
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.mutation.addNode
import io.github.chandu4221.m3stage.mutation.removeNode
import io.github.chandu4221.m3stage.mutation.updateProp
import io.github.chandu4221.m3stage.property.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DesignNodeOperationsTest {

    private val textNode = DesignNode(
        id = NodeId("text-1"),
        kind = ComponentKind.Text,
        props = mapOf(ComponentCatalog.TextProps.TextContent.id to PropertyValue.StringValue("Hello"))
    )

    private val columnNode = DesignNode(
        id = NodeId("col-1"),
        kind = ComponentKind.Column,
        children = listOf(textNode)
    )

    private val rootNode = DesignNode(
        id = NodeId("root"),
        kind = ComponentKind.Scaffold,
        children = listOf(columnNode)
    )

    @Test
    fun testFindNode() {
        assertNotNull(rootNode.findNode(NodeId("root")))
        assertNotNull(rootNode.findNode(NodeId("col-1")))
        assertNotNull(rootNode.findNode(NodeId("text-1")))
        assertNull(rootNode.findNode(NodeId("non-existent")))
    }

    @Test
    fun testFindParent() {
        assertNull(rootNode.findParent(NodeId("root")))
        assertEquals(NodeId("root"), rootNode.findParent(NodeId("col-1"))?.id)
        assertEquals(NodeId("col-1"), rootNode.findParent(NodeId("text-1"))?.id)
    }

    @Test
    fun testPathIdsTo() {
        val path = rootNode.pathIdsTo(NodeId("text-1"))
        assertEquals(listOf(NodeId("root"), NodeId("col-1"), NodeId("text-1")), path)
    }

    @Test
    fun testAddNodePreservesImmutability() {
        val newNode = DesignNode(id = NodeId("button-1"), kind = ComponentKind.Button)
        val updatedTree = rootNode.addNode(NodeId("col-1"), newNode)

        assertEquals(1, rootNode.findNode(NodeId("col-1"))?.children?.size)
        assertEquals(2, updatedTree.findNode(NodeId("col-1"))?.children?.size)
        assertNotNull(updatedTree.findNode(NodeId("button-1")))
    }

    @Test
    fun testRemoveNode() {
        val updatedTree = rootNode.removeNode(NodeId("text-1"))

        assertNotNull(rootNode.findNode(NodeId("text-1")))
        assertNull(updatedTree.findNode(NodeId("text-1")))
    }

    @Test
    fun testUpdateProp() {
        val updatedTree = rootNode.updateProp(
            nodeId = NodeId("text-1"),
            key = ComponentCatalog.TextProps.TextContent,
            value = PropertyValue.StringValue("Updated Text")
        )

        val originalText = rootNode.findNode(NodeId("text-1"))?.get(ComponentCatalog.TextProps.TextContent)?.value
        val newText = updatedTree.findNode(NodeId("text-1"))?.get(ComponentCatalog.TextProps.TextContent)?.value

        assertEquals("Hello", originalText)
        assertEquals("Updated Text", newText)
    }
}