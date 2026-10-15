package io.github.chandu4221.m3stage.adapter.renderer

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.DesignNode
import io.github.chandu4221.m3stage.model.NodeId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaceholderAlphaPredicateTest {

    private fun createNode(kind: ComponentKind, isPrefilled: Boolean): DesignNode {
        return DesignNode(
            id = NodeId("test_${kind.name.lowercase()}"),
            kind = kind,
            isPrefilled = isPrefilled
        )
    }

    @Test
    fun testTextInsideColumnReturnsTrue() {
        val node = createNode(ComponentKind.Text, isPrefilled = true)
        val ancestors = listOf(ComponentKind.Column)
        assertTrue(shouldRenderAsPlaceholder(node, ancestors), "Prefilled Text inside Column must receive placeholder alpha")
    }

    @Test
    fun testTextInsideButtonReturnsFalse() {
        val node = createNode(ComponentKind.Text, isPrefilled = true)
        val ancestors = listOf(ComponentKind.Column, ComponentKind.Button)
        assertFalse(shouldRenderAsPlaceholder(node, ancestors), "Prefilled Text inside Button must NOT receive alpha")
    }

    @Test
    fun testButtonItselfReturnsFalse() {
        val node = createNode(ComponentKind.Button, isPrefilled = true)
        val ancestors = listOf(ComponentKind.Column)
        assertFalse(shouldRenderAsPlaceholder(node, ancestors), "Interactive Button must NOT receive placeholder alpha")
    }

    @Test
    fun testNavigationBarItemReturnsFalse() {
        val node = createNode(ComponentKind.NavigationBarItem, isPrefilled = true)
        val ancestors = listOf(ComponentKind.NavigationBar)
        assertFalse(shouldRenderAsPlaceholder(node, ancestors), "NavigationBarItem must NOT receive placeholder alpha")
    }

    @Test
    fun testStandaloneIconPrefilledReturnsTrue() {
        val node = createNode(ComponentKind.Icon, isPrefilled = true)
        val ancestors = listOf(ComponentKind.Box)
        assertTrue(shouldRenderAsPlaceholder(node, ancestors), "Standalone prefilled Icon in Box is scaffolding and must receive alpha")
    }

    @Test
    fun testIconInsideInteractiveWidgetReturnsFalse() {
        val iconInFab = createNode(ComponentKind.Icon, isPrefilled = true)
        assertFalse(
            shouldRenderAsPlaceholder(iconInFab, listOf(ComponentKind.FloatingActionButton)),
            "Icon inside FAB must NOT receive alpha"
        )

        val iconInNavItem = createNode(ComponentKind.Icon, isPrefilled = true)
        assertFalse(
            shouldRenderAsPlaceholder(iconInNavItem, listOf(ComponentKind.NavigationBar, ComponentKind.NavigationBarItem)),
            "Icon inside NavigationBarItem must NOT receive alpha"
        )
    }

    @Test
    fun testNonPrefilledNodesAlwaysReturnFalse() {
        val authoredText = createNode(ComponentKind.Text, isPrefilled = false)
        assertFalse(shouldRenderAsPlaceholder(authoredText, listOf(ComponentKind.Column)))

        val authoredIcon = createNode(ComponentKind.Icon, isPrefilled = false)
        assertFalse(shouldRenderAsPlaceholder(authoredIcon, emptyList()))
    }
}
