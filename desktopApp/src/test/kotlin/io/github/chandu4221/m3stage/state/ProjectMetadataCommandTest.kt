package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.model.ProjectId
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectMetadataCommandTest {

    private fun createSampleProject(): Project {
        return Project(
            id = ProjectId("p-test"),
            name = "OldName",
            basePackage = "com.old.app"
        )
    }

    @Test
    fun testUpdateProjectMetaCommandExecutionAndUndo() {
        val project = createSampleProject()
        val command = UpdateProjectMetaCommand(
            oldName = project.name,
            oldBasePackage = project.basePackage,
            newName = "NewName",
            newBasePackage = "com.new.app"
        )

        // Execute
        val executed = command.execute(project)
        assertEquals("NewName", executed.name)
        assertEquals("com.new.app", executed.basePackage)

        // Undo
        val undone = command.undo(executed)
        assertEquals("OldName", undone.name)
        assertEquals("com.old.app", undone.basePackage)
    }

    @Test
    fun testUpdateProjectMetaCommandWhitespaceTrimming() {
        val project = createSampleProject()
        val command = UpdateProjectMetaCommand(
            oldName = project.name,
            oldBasePackage = project.basePackage,
            newName = "   Trimmed Name   ",
            newBasePackage = "   com.trimmed.pkg   "
        )

        val executed = command.execute(project)
        assertEquals("Trimmed Name", executed.name)
        assertEquals("com.trimmed.pkg", executed.basePackage)
    }
}
