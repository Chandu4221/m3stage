package io.github.chandu4221.m3stage.component

import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.Screen
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.validation.ProjectValidation
import kotlin.test.Test
import kotlin.test.assertTrue

class GenesisValidationTest {

    private class TestDeterministicIdGenerator : IdGenerator {
        private var counter = 1
        override fun nextNodeId(): NodeId = NodeId("node-${counter++}")
        override fun nextScreenId(): ScreenId = ScreenId("screen-1")
        override fun nextProjectId(): ProjectId = ProjectId("proj-1")
    }

    @Test
    fun testAllComponentGenesisSubtreesAreValid() {
        for (def in ComponentCatalog.all) {
            val idGen = TestDeterministicIdGenerator()
            val subtree = def.createDefaultSubtree(idGen)
            val project = Project(
                id = ProjectId("test-proj"),
                name = "Test",
                basePackage = "com.example",
                screens = listOf(
                    Screen(
                        id = ScreenId("screen-1"),
                        name = "Screen1",
                        route = "/",
                        root = subtree
                    )
                )
            )

            val errors = ProjectValidation.validate(project)
            assertTrue(
                errors.isEmpty(),
                "Genesis of ${def.kind} is invalid: ${errors.joinToString("; ")}"
            )
        }
    }
}
