package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.ui.organism.parseHexColor
import io.github.chandu4221.m3stage.ui.organism.shortName
import io.github.chandu4221.m3stage.ui.theme.SeedColorPresets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ThemeDrawerTest {

    private fun createSampleProject(): Project {
        return Project(
            id = ProjectId("p-test"),
            name = "TestProject",
            basePackage = "com.example.test",
            seedColor = 0xFF6750A4L,
            isDarkMode = false
        )
    }

    @Test
    fun testUpdateThemeCommandExecutionAndUndo() {
        val project = createSampleProject()
        val command = UpdateThemeCommand(
            oldSeedColor = project.seedColor,
            oldIsDarkMode = project.isDarkMode,
            newSeedColor = 0xFF0061A4L, // Sapphire Blue
            newIsDarkMode = true
        )

        // Execute
        val executed = command.execute(project)
        assertEquals(0xFF0061A4L, executed.seedColor)
        assertEquals(true, executed.isDarkMode)

        // Undo
        val undone = command.undo(executed)
        assertEquals(0xFF6750A4L, undone.seedColor)
        assertEquals(false, undone.isDarkMode)
    }

    @Test
    fun testParseHexColorValidInputs() {
        assertEquals(0xFF6750A4L, parseHexColor("#6750A4"))
        assertEquals(0xFF6750A4L, parseHexColor("6750A4"))
        assertEquals(0xFF6750A4L, parseHexColor("0x6750A4"))
        assertEquals(0xFF0061A4L, parseHexColor("#0061a4"))
        assertEquals(0xFF000000L, parseHexColor("#000000"))
        assertEquals(0xFFFFFFFFL, parseHexColor("#FFFFFF"))
        assertEquals(0x806750A4L, parseHexColor("#806750A4"))
    }

    @Test
    fun testParseHexColorInvalidInputs() {
        assertNull(parseHexColor(""))
        assertNull(parseHexColor("#"))
        assertNull(parseHexColor("123"))
        assertNull(parseHexColor("#12345"))
        assertNull(parseHexColor("ZZZZZZ"))
        assertNull(parseHexColor("#123456789"))
    }

    @Test
    fun testSeedColorPresetShortNames() {
        assertEquals("Purple", SeedColorPresets.BaselinePurple.shortName())
        assertEquals("Blue", SeedColorPresets.SapphireBlue.shortName())
        assertEquals("Green", SeedColorPresets.EmeraldGreen.shortName())
        assertEquals("Coral", SeedColorPresets.CoralRed.shortName())
        assertEquals("Amber", SeedColorPresets.SunsetOrange.shortName())
        assertEquals("Teal", SeedColorPresets.OceanTeal.shortName())
        assertEquals("Pink", SeedColorPresets.RosePink.shortName())
        assertEquals("Mono", SeedColorPresets.SlateMono.shortName())
    }
}
