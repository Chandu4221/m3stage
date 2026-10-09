package io.github.chandu4221.m3stage.adapter.codegen

object KotlinIdentifierSanitizer {

    /**
     * Converts an arbitrary user-facing screen name (e.g. "Screen 1", "my-profile", "1st-tab")
     * into a valid, idiomatic PascalCase Kotlin identifier.
     */
    fun toPascalCase(rawName: String): String {
        val trimmed = rawName.trim()
        if (trimmed.isEmpty()) return "Screen"

        // Split on non-alphanumeric characters (spaces, hyphens, punctuation, symbols)
        val words = trimmed.split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotEmpty() }

        if (words.isEmpty()) return "Screen"

        val pascal = words.joinToString("") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        // If the resulting identifier starts with a digit, prefix with 'Screen'
        return if (pascal.first().isDigit()) {
            "Screen$pascal"
        } else {
            pascal
        }
    }

    /**
     * Resolves the Composable function name for a screen.
     * Avoids awkward double suffixes (e.g. "HomeScreenScreen").
     */
    fun toFunctionName(screenIdentifier: String): String {
        return if (screenIdentifier.endsWith("Screen")) {
            screenIdentifier
        } else {
            "${screenIdentifier}Screen"
        }
    }
}