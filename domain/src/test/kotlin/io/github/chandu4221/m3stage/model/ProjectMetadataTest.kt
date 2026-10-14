package io.github.chandu4221.m3stage.model

import io.github.chandu4221.m3stage.mutation.updateMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectMetadataTest {

    @Test
    fun testValidPackageNames() {
        val validPackages = listOf(
            "com.example.app",
            "org.sample",
            "io.github.chandu4221.m3stage",
            "app",
            "com.example_app.module_1",
            "_internal.pkg",
            "com.example.App"
        )
        for (pkg in validPackages) {
            assertTrue(Project.isValidPackageName(pkg), "Expected '$pkg' to be a valid package name")
            assertTrue(Project.isValidPackageName("  $pkg  "), "Expected '$pkg' with whitespace to be valid after trim")
        }
    }

    @Test
    fun testInvalidPackageNames() {
        val invalidPackages = listOf(
            "",
            "   ",
            "123.abc",
            "com..example",
            "com.example.",
            ".com.example",
            "com.example-app",
            "com.example app",
            "com.example@app",
            "com.123start",
            "com.example/app"
        )
        for (pkg in invalidPackages) {
            assertFalse(Project.isValidPackageName(pkg), "Expected '$pkg' to be an invalid package name")
        }
    }

    @Test
    fun testProjectNameValidation() {
        assertTrue(Project.isValidProjectName("My App"))
        assertTrue(Project.isValidProjectName("Stage_123"))
        assertFalse(Project.isValidProjectName(""))
        assertFalse(Project.isValidProjectName("   "))
    }

    @Test
    fun testProjectUpdateMetadataMutation() {
        val project = Project(
            id = ProjectId("p-1"),
            name = "Initial Name",
            basePackage = "com.initial.app"
        )

        val updated = project.updateMetadata("New Name", "com.new.app")
        assertEquals("New Name", updated.name)
        assertEquals("com.new.app", updated.basePackage)
        assertEquals(project.id, updated.id)

        // Test whitespace trimming
        val trimmed = project.updateMetadata("   Trimmed App   ", "   com.trimmed.app   ")
        assertEquals("Trimmed App", trimmed.name)
        assertEquals("com.trimmed.app", trimmed.basePackage)
    }
}
