package io.github.chandu4221.m3stage.adapter.codegen.util

object KotlinIdentifierSanitizer {

    private val KEYWORDS = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if",
        "in", "interface", "is", "null", "object", "package", "return", "super", "this",
        "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while"
    )

    fun toPascalCase(input: String): String {
        // If input has no delimiters (underscores, dashes, spaces), preserve existing camel/pascal casing
        if (!input.contains(Regex("[ _-]"))) {
            val sanitized = input.replace(Regex("[^a-zA-Z0-9]"), "")
            return when {
                sanitized.isEmpty() || sanitized.first().isDigit() -> "Screen$sanitized"
                else -> sanitized.replaceFirstChar { it.uppercase() }
            }
        }

        val sanitized = input.replace(Regex("[^a-zA-Z0-9]"), " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString("") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        return if (sanitized.isEmpty() || sanitized.first().isDigit()) {
            "Screen${sanitized}"
        } else {
            sanitized
        }
    }

    fun toFunctionName(input: String): String {
        val pascal = toPascalCase(input)
        return if (KEYWORDS.contains(pascal.lowercase())) {
            "`$pascal`"
        } else {
            pascal
        }
    }
}
