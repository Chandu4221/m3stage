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
                DesignNode(id = dupId, kind = ComponentKind.TopAppBar),
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
                DesignNode(id = NodeId("top-bar"), kind = ComponentKind.TopAppBar),
                DesignNode(id = NodeId("col"), kind = ComponentKind.Column)
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
        assertTrue(errors.isEmpty())
    }
}