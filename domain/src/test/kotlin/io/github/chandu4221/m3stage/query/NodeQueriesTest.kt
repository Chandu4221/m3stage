package io.github.chandu4221.m3stage.query

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.component.definition.ScaffoldDefinition
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NodeQueriesTest {

    @Test
    fun testFindNearestAcceptingAncestorWalksUpToContainer() {
        val textNode = DesignNode(
            id = NodeId("text-1"),
            kind = ComponentKind.Text
        )
        val columnNode = DesignNode(
            id = NodeId("col-1"),
            kind = ComponentKind.Column,
            children = listOf(textNode)
        )
        val scaffoldNode = DesignNode(
            id = NodeId("scaffold-1"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(
                ScaffoldDefinition.ContentSlot.id to listOf(columnNode)
            )
        )

        // Text cannot accept Card, but Column (parent) can accept Card!
        val nearestForCard = textNode.findNearestAcceptingAncestor(scaffoldNode, ComponentKind.Card)
        assertNotNull(nearestForCard)
        assertEquals(NodeId("col-1"), nearestForCard.id, "Nearest ancestor for Card from Text must be Column")

        // Column directly accepts Button
        val nearestForButton = columnNode.findNearestAcceptingAncestor(scaffoldNode, ComponentKind.Button)
        assertNotNull(nearestForButton)
        assertEquals(NodeId("col-1"), nearestForButton.id, "Nearest ancestor for Button from Column must be Column itself")
    }
}
