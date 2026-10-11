package io.github.chandu4221.m3stage.adapter.codegen

import io.github.chandu4221.m3stage.adapter.codegen.util.KotlinIdentifierSanitizer
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinIdentifierSanitizerTest {

    @Test
    fun testPascalCaseConversion() {
        assertEquals("HomeScreen", KotlinIdentifierSanitizer.toPascalCase("home screen"))
        assertEquals("UserProfile", KotlinIdentifierSanitizer.toPascalCase("user_profile"))
        assertEquals("DashboardScreen", KotlinIdentifierSanitizer.toPascalCase("dashboard-screen"))
        assertEquals("Screen123Screen", KotlinIdentifierSanitizer.toPascalCase("123 screen"))
    }

    @Test
    fun testReservedKeywordEscaping() {
        assertEquals("`Package`", KotlinIdentifierSanitizer.toFunctionName("package"))
        assertEquals("`Class`", KotlinIdentifierSanitizer.toFunctionName("class"))
        assertEquals("HomeScreen", KotlinIdentifierSanitizer.toFunctionName("home screen"))
    }
}
