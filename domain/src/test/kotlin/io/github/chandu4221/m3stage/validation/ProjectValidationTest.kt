package io.github.chandu4221.m3stage.validation

import io.github.chandu4221.m3stage.component.ComponentKind
import io.github.chandu4221.m3stage.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectValidationTest {

    @Test
    fun testEmptyScreensProducesError() {
        val emptyProject = Project(
            id = ProjectId("proj-1"),
            name = "Test",
            basePackage = "com.example",
            screens = emptyList()
        )

        val errors = ProjectValidation.validate(emptyProject)
        assertEquals(1, errors.size)
        assertTrue(errors.first().contains("Project must contain at least one screen"))
    }

    @Test
    fun testDuplicateNodeIdsProducesError() {
        val dupId = NodeId("dup-node")
        val rootNode = DesignNode(
            id = NodeId("root"),
            kind = ComponentKind.Scaffold,
            children = listOf(
                DesignNode(id = dupId, kind = ComponentKind.Button),
                DesignNode(
                    id = NodeId("col-1"),
                    kind = ComponentKind.Column,
                    children = listOf(
                        DesignNode(id = dupId, kind = ComponentKind.Text)
                    )
                )
            )
        )

        val project = Project(
            id = ProjectId("proj-1"),
            name = "Test",
            basePackage = "com.example",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Home", route = "/", root = rootNode)
            )
        )

        val errors = ProjectValidation.validate(project)
        assertEquals(1, errors.size)
        assertTrue(errors.first().contains("Duplicate Node ID detected: 'dup-node'"))
    }

    @Test
    fun testValidProjectPasses() {
        val rootNode = DesignNode(
            id = NodeId("root"),
            kind = ComponentKind.Scaffold,
            children = listOf(
                DesignNode(id = NodeId("col"), kind = ComponentKind.Column)
            ),
            slots = mapOf(
                io.github.chandu4221.m3stage.component.slot.SlotId("topBar") to listOf(
                    DesignNode(id = NodeId("top-bar"), kind = ComponentKind.TopAppBar)
                )
            )
        )

        val project = Project(
            id = ProjectId("proj-1"),
            name = "Test",
            basePackage = "com.example",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Home", route = "/", root = rootNode)
            )
        )

        val errors = ProjectValidation.validate(project)
        assertTrue(errors.isEmpty(), "Expected no errors but got: $errors")
    }

    @Test
    fun testSlotOnlyPlacement() {
        // TopAppBar as child of Column -> validation error
        val invalidRoot = DesignNode(
            id = NodeId("col-1"),
            kind = ComponentKind.Column,
            children = listOf(
                DesignNode(id = NodeId("top-bar"), kind = ComponentKind.TopAppBar)
            )
        )
        val invalidProject = Project(
            id = ProjectId("proj-1"),
            name = "Test",
            basePackage = "com.example",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Home", route = "/", root = invalidRoot)
            )
        )
        val errors = ProjectValidation.validate(invalidProject)
        assertTrue(errors.any { it.contains("slot-only") })

        // TopAppBar in Scaffold's topBar slot -> no error
        val validRoot = DesignNode(
            id = NodeId("root"),
            kind = ComponentKind.Scaffold,
            slots = mapOf(
                io.github.chandu4221.m3stage.component.slot.SlotId("topBar") to listOf(
                    DesignNode(id = NodeId("top-bar"), kind = ComponentKind.TopAppBar)
                )
            )
        )
        val validProject = Project(
            id = ProjectId("proj-2"),
            name = "Test",
            basePackage = "com.example",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Home", route = "/", root = validRoot)
            )
        )
        val validErrors = ProjectValidation.validate(validProject)
        assertTrue(validErrors.isEmpty(), "Expected no errors for TopAppBar in topBar slot, got: $validErrors")
    }

    @Test
    fun testDualContainmentProducesError() {
        val childId = NodeId("child-1")
        val childNode = DesignNode(id = childId, kind = ComponentKind.TopAppBar)
        val rootNode = DesignNode(
            id = NodeId("root"),
            kind = ComponentKind.Scaffold,
            children = listOf(childNode),
            slots = mapOf(
                io.github.chandu4221.m3stage.component.slot.SlotId("topBar") to listOf(childNode)
            )
        )

        val project = Project(
            id = ProjectId("proj-1"),
            name = "Test",
            basePackage = "com.example",
            screens = listOf(
                Screen(id = ScreenId("screen-1"), name = "Home", route = "/", root = rootNode)
            )
        )

        val errors = ProjectValidation.validate(project)
        assertTrue(errors.any { it.contains("both children and slot") })
    }
}