package io.github.chandu4221.m3stage.adapter.renderer.selection

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectionTargetResolverTest {

    @Test
    fun testFigmaDrillDownSequenceExactAcceptance1() {
        val text = DesignNode(id = NodeId("text"), kind = ComponentKind.Text)
        val innerColumn = DesignNode(id = NodeId("inner-column"), kind = ComponentKind.Column, children = listOf(text))
        val card = DesignNode(id = NodeId("card"), kind = ComponentKind.Card, children = listOf(innerColumn))
        val contentColumn = DesignNode(id = NodeId("content-column"), kind = ComponentKind.Column, children = listOf(card))
        val scaffold = DesignNode(id = NodeId("scaffold"), kind = ComponentKind.Scaffold, children = listOf(contentColumn))

        val treeInfo = scaffold.toTreeInfo()

        val bounds = mapOf(
            scaffold.id to Rect(0f, 0f, 400f, 800f),
            contentColumn.id to Rect(0f, 64f, 400f, 764f),
            card.id to Rect(16f, 80f, 384f, 280f),
            innerColumn.id to Rect(24f, 88f, 376f, 268f),
            text.id to Rect(32f, 96f, 132f, 126f)
        )
        val point = Offset(50f, 100f)

        // With anchor at contentColumn (the screen root's main content body)
        // 1. Single click Text -> outermost container under contentColumn = card
        val (selCard, ancCard) = SelectionTargetResolver.resolveSingleClickTarget(
            point = point,
            anchor = contentColumn.id,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(card.id, selCard)
        assertEquals(contentColumn.id, ancCard)

        // 2. Double-click Text from contentColumn anchor:
        // anchor becomes card, deepest node under cursor inside Card = text
        val (dbSel, dbAnc) = SelectionTargetResolver.resolveDoubleClickTarget(
            point = point,
            anchor = contentColumn.id,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(text.id, dbSel)
        assertEquals(card.id, dbAnc)

        // 3. Single-click the text again with anchor = card:
        // outermost container within card's subtree containing point = innerColumn
        val (innerSel, innerAnc) = SelectionTargetResolver.resolveSingleClickTarget(
            point = point,
            anchor = dbAnc,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(innerColumn.id, innerSel)
        assertEquals(card.id, innerAnc)

        // 4. ESC from anchor = card:
        // anchor = parent(card) = contentColumn, selection unchanged
        val (escSel, escAnc) = SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = innerAnc,
            selectedNodeId = innerSel,
            screenRootId = scaffold.id,
            treeInfo = treeInfo
        )
        assertEquals(innerColumn.id, escSel)
        assertEquals(contentColumn.id, escAnc)
    }

    @Test
    fun testEmptyArtboardClickDeselects() {
        val scaffold = DesignNode(id = NodeId("scaffold"), kind = ComponentKind.Scaffold)
        val treeInfo = scaffold.toTreeInfo()
        val bounds = mapOf(scaffold.id to Rect(0f, 0f, 400f, 800f))

        val (sel, anc) = SelectionTargetResolver.resolveSingleClickTarget(
            point = Offset(500f, 900f),
            anchor = scaffold.id,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertNull(sel)
        assertEquals(scaffold.id, anc)
    }
}
