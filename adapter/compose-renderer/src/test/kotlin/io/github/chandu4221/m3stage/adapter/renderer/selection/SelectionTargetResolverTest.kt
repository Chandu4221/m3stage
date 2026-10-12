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
        val innerBox = DesignNode(id = NodeId("inner-box"), kind = ComponentKind.Box, children = listOf(text))
        val innerColumn = DesignNode(id = NodeId("inner-column"), kind = ComponentKind.Column, children = listOf(innerBox))
        val card = DesignNode(id = NodeId("card"), kind = ComponentKind.Card, children = listOf(innerColumn))
        val contentColumn = DesignNode(id = NodeId("content-column"), kind = ComponentKind.Column, children = listOf(card))
        val scaffold = DesignNode(id = NodeId("scaffold"), kind = ComponentKind.Scaffold, children = listOf(contentColumn))

        val treeInfo = scaffold.toTreeInfo()

        val bounds = mapOf(
            scaffold.id to Rect(0f, 0f, 400f, 800f),
            contentColumn.id to Rect(0f, 64f, 400f, 764f),
            card.id to Rect(16f, 80f, 384f, 280f),
            innerColumn.id to Rect(24f, 88f, 376f, 268f),
            innerBox.id to Rect(28f, 92f, 370f, 260f),
            text.id to Rect(32f, 96f, 132f, 126f)
        )
        val point = Offset(50f, 100f)

        // Starting anchor = scaffold (Screen root)
        // 1. Single click on Text -> outermost container under scaffold containing point = contentColumn
        val (sel1, anc1) = SelectionTargetResolver.resolveSingleClickTarget(
            point = point,
            anchor = scaffold.id,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(contentColumn.id, sel1)
        assertEquals(scaffold.id, anc1)

        // 2. Double click -> drills from contentColumn into next level: card
        val (sel2, anc2) = SelectionTargetResolver.resolveDoubleClickTarget(
            point = point,
            anchor = anc1,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(card.id, sel2)
        assertEquals(contentColumn.id, anc2)

        // 3. Double click again -> drills from card into next level: innerColumn
        val (sel3, anc3) = SelectionTargetResolver.resolveDoubleClickTarget(
            point = point,
            anchor = anc2,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(innerColumn.id, sel3)
        assertEquals(card.id, anc3)

        // 4. Double click again -> drills from innerColumn into next level: innerBox
        val (sel4, anc4) = SelectionTargetResolver.resolveDoubleClickTarget(
            point = point,
            anchor = anc3,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(innerBox.id, sel4)
        assertEquals(innerColumn.id, anc4)

        // 5. Double click again -> drills from innerBox into leaf: text
        val (sel5, anc5) = SelectionTargetResolver.resolveDoubleClickTarget(
            point = point,
            anchor = anc4,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(text.id, sel5)
        assertEquals(innerBox.id, anc5)

        // 6. Double click on leaf -> stays on leaf: text
        val (sel6, anc6) = SelectionTargetResolver.resolveDoubleClickTarget(
            point = point,
            anchor = anc5,
            screenRootId = scaffold.id,
            treeInfo = treeInfo,
            bounds = bounds
        )
        assertEquals(text.id, sel6)
        assertEquals(innerBox.id, anc6)

        // 7. ESC pops anchor up:
        // innerBox -> innerColumn -> card -> contentColumn -> scaffold -> deselect
        val (esc1Sel, esc1Anc) = SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = anc5,
            selectedNodeId = sel5,
            screenRootId = scaffold.id,
            treeInfo = treeInfo
        )
        assertEquals(text.id, esc1Sel)
        assertEquals(innerColumn.id, esc1Anc)

        val (esc2Sel, esc2Anc) = SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = esc1Anc,
            selectedNodeId = esc1Sel,
            screenRootId = scaffold.id,
            treeInfo = treeInfo
        )
        assertEquals(card.id, esc2Anc)

        val (esc3Sel, esc3Anc) = SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = esc2Anc,
            selectedNodeId = esc2Sel,
            screenRootId = scaffold.id,
            treeInfo = treeInfo
        )
        assertEquals(contentColumn.id, esc3Anc)

        val (esc4Sel, esc4Anc) = SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = esc3Anc,
            selectedNodeId = esc3Sel,
            screenRootId = scaffold.id,
            treeInfo = treeInfo
        )
        assertEquals(scaffold.id, esc4Anc)

        // ESC at root deselects
        val (esc5Sel, esc5Anc) = SelectionTargetResolver.resolveEscapeAction(
            currentAnchor = esc4Anc,
            selectedNodeId = esc4Sel,
            screenRootId = scaffold.id,
            treeInfo = treeInfo
        )
        assertNull(esc5Sel)
        assertEquals(scaffold.id, esc5Anc)
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
