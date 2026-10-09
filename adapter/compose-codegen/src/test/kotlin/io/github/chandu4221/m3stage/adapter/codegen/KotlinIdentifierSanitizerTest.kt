package io.github.chandu4221.m3stage.adapter.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinIdentifierSanitizerTest {

    @Test
    fun testToPascalCaseStandardNames() {
        assertEquals("Screen1", KotlinIdentifierSanitizer.toPascalCase("Screen 1"))
        assertEquals("Home", KotlinIdentifierSanitizer.toPascalCase("Home"))
        assertEquals("MyProfile", KotlinIdentifierSanitizer.toPascalCase("my-profile"))
        assertEquals("OrderDetailsPage", KotlinIdentifierSanitizer.toPascalCase("order_details_page"))
    }

    @Test
    fun testToPascalCaseLeadingDigitPrefix() {
        assertEquals("Screen1Home", KotlinIdentifierSanitizer.toPascalCase("1Home"))
        assertEquals("Screen2ndTab", KotlinIdentifierSanitizer.toPascalCase("2nd-tab"))
    }

    @Test
    fun testToPascalCaseBlankFallback() {
        assertEquals("Screen", KotlinIdentifierSanitizer.toPascalCase("   "))
        assertEquals("Screen", KotlinIdentifierSanitizer.toPascalCase("!@#$%^"))
    }

    @Test
    fun testToFunctionNamePreventsDoubleSuffix() {
        assertEquals("HomeScreen", KotlinIdentifierSanitizer.toFunctionName("Home"))
        assertEquals("HomeScreen", KotlinIdentifierSanitizer.toFunctionName("HomeScreen"))
        assertEquals("Screen1Screen", KotlinIdentifierSanitizer.toFunctionName("Screen1"))
    }
}