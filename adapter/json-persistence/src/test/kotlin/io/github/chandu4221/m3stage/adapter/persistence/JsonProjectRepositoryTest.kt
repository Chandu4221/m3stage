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

    @Test
    fun testUnknownComponentKindQuarantinesProject(): Unit = runBlocking {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "m3stage_test_typo_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        val typoFile = File(tempDir, "project.json")
        typoFile.writeText(
            """
            {
                "id": "proj-1",
                "name": "Typo Project",
                "basePackage": "com.test",
                "defaultDevice": "Pixel8",
                "screens": [
                    {
                        "id": "s-1",
                        "name": "Screen1",
                        "route": "/s1",
                        "device": "Pixel8",
                        "root": {
                            "id": "root-1",
                            "type": "Scaffod",
                            "props": {},
                            "modifiers": [],
                            "children": [],
                            "slots": {},
                            "isVisible": true
                        }
                    }
                ],
                "seedColor": 0,
                "isDarkMode": false
            }
            """.trimIndent()
        )

        val repository = JsonProjectRepository(typoFile)

        val exception = assertFailsWith<CorruptProjectException> {
            repository.load()
        }

        assertTrue(exception.cause is UnknownComponentKindException, "Cause must be UnknownComponentKindException")
        assertNotNull(exception.backupFile)
        assertTrue(exception.backupFile.exists(), "Quarantined corrupt backup file must be created")

        // Clean up
        tempDir.deleteRecursively()
    }

    @Test
    fun testMissingSchemaVersionDefaultsToCurrentVersion(): Unit = runBlocking {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "m3stage_test_default_schema_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        val legacyFile = File(tempDir, "project.json")
        legacyFile.writeText(
            """
            {
                "id": "proj-legacy",
                "name": "Legacy Project",
                "basePackage": "com.test",
                "screens": []
            }
            """.trimIndent()
        )

        val repository = JsonProjectRepository(legacyFile)
        val project = repository.load()
        assertNotNull(project)

        tempDir.deleteRecursively()
    }

    @Test
    fun testFutureSchemaVersionThrowsCorruptProjectException(): Unit = runBlocking {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "m3stage_test_future_schema_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        val futureFile = File(tempDir, "project.json")
        futureFile.writeText(
            """
            {
                "id": "proj-future",
                "name": "Future Project",
                "basePackage": "com.test",
                "schemaVersion": 99,
                "screens": []
            }
            """.trimIndent()
        )

        val repository = JsonProjectRepository(futureFile)
        val exception = assertFailsWith<CorruptProjectException> {
            repository.load()
        }

        assertTrue(exception.cause is IllegalArgumentException)
        assertTrue(exception.cause?.message?.contains("newer M3Stage version") == true)
        assertNotNull(exception.backupFile)
        assertTrue(exception.backupFile.exists())

        tempDir.deleteRecursively()
    }
}