package io.github.chandu4221.m3stage.adapter.persistence

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JsonProjectRepositoryTest {

    @Test
    fun testCorruptProjectFileQuarantineAndBackup(): Unit = runBlocking {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "m3stage_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        val corruptFile = File(tempDir, "project.json")
        corruptFile.writeText("{ this is invalid json content !!! }")

        val repository = JsonProjectRepository(corruptFile)

        val exception = assertFailsWith<CorruptProjectException> {
            repository.load()
        }

        assertNotNull(exception.backupFile)
        assertTrue(exception.backupFile.exists(), "Backup quarantine file must exist on disk")
        assertTrue(exception.backupFile.readText().contains("invalid json content"))

        // Clean up
        tempDir.deleteRecursively()
    }
}