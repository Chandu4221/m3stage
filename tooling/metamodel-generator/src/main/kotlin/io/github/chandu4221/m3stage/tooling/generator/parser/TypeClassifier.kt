package io.github.chandu4221.m3stage.tooling.generator.parser

import io.github.chandu4221.m3stage.tooling.generator.model.PropertyKind
import io.github.chandu4221.m3stage.tooling.generator.model.RawCallback
import io.github.chandu4221.m3stage.tooling.generator.model.RawSlot

object TypeClassifier {

    private val KNOWN_RECEIVER_SCOPES = setOf(
        "androidx.compose.foundation.layout.RowScope",
        "androidx.compose.foundation.layout.ColumnScope",
        "androidx.compose.foundation.layout.BoxScope",
        "androidx.compose.material3.AppBarColumnScope",
        "androidx.compose.material3.AppBarRowScope"
    )

    fun isModifier(rawType: String, paramName: String): Boolean {
        return paramName == "modifier" || rawType.contains("androidx.compose.ui.Modifier")
    }

    /**
     * Issue 3: Checks if a parameter represents an extension function receiver.
     */
    fun isExtensionReceiver(paramName: String, rawType: String): Boolean {
        return paramName.startsWith("\$this\$") ||
                paramName == "receiver" ||
                KNOWN_RECEIVER_SCOPES.contains(rawType) ||
                rawType.endsWith("Scope") && !paramName.contains("Scope")
    }

    fun extractReceiverSimpleName(rawType: String): String {
        return rawType.split(".").last().removeSuffix("?")
    }

    /**
     * Issue 5: Distinguishes true Composable lambda slots from non-composable callbacks.
     * In Metalava, composable lambdas have an @Composable annotation or composable composer convention.
     */
    fun isComposableSlot(rawType: String, paramName: String): Boolean {
        if (!rawType.contains("kotlin.jvm.functions.Function")) return false

        // Drawing / non-composable lambdas
        if (paramName.startsWith("draw") || paramName == "shapeCalc") return false

        // If it's a known callback name, it's NOT a slot
        if (isCallback(paramName, rawType)) return false

        return rawType.contains("kotlin.Unit") || rawType.endsWith("Unit>")
    }

    /**
     * Issue 5: Detects ordinary callbacks (onClick, onCheckedChange, onValueChange, etc.)
     */
    fun isCallback(paramName: String, rawType: String): Boolean {
        if (!rawType.contains("kotlin.jvm.functions.Function")) return false
        val lower = paramName.lowercase()
        return lower.startsWith("on") ||
                lower.startsWith("draw") ||
                lower.contains("click") ||
                lower.contains("change") ||
                lower.contains("dismiss") ||
                lower.contains("request") ||
                lower.contains("action")
    }

    /**
     * Issue 5 & 9: Extracts slot metadata with exact scope and arguments.
     */
    fun extractSlot(paramName: String, rawType: String, isOptional: Boolean): RawSlot {
        val isNullable = rawType.contains("?")
        val scope = extractSlotScope(rawType)
        val args = extractSlotArgs(rawType)
        val isRequired = !isOptional && !isNullable

        return RawSlot(
            name = paramName,
            isRequired = isRequired,
            isNullable = isNullable,
            scope = scope,
            args = args
        )
    }

    fun extractCallback(paramName: String, rawType: String, isOptional: Boolean): RawCallback {
        return RawCallback(
            name = paramName,
            signature = cleanTypeDisplay(rawType),
            isOptional = isOptional
        )
    }

    private fun extractSlotScope(rawType: String): String? {
        // e.g. Function1<? super androidx.compose.foundation.layout.RowScope, kotlin.Unit>
        val regex = Regex("super\\s+([a-zA-Z0-9_.]+Scope)")
        val match = regex.find(rawType)
        return match?.groupValues?.get(1)?.split(".")?.last()
    }

    private fun extractSlotArgs(rawType: String): List<String> {
        // Extracts non-scope argument types (e.g. PaddingValues in Scaffold content)
        if (rawType.contains("PaddingValues")) return listOf("PaddingValues")
        if (rawType.contains("SliderState")) return listOf("SliderState")
        if (rawType.contains("TextFieldState")) return listOf("TextFieldState")
        return emptyList()
    }

    /**
     * Issue 6: Maps types strictly through resolved types rather than naive substring matching.
     */
    fun classifyProperty(paramName: String, rawType: String): Pair<PropertyKind, String?> {
        val clean = rawType.removeSuffix("?").trim()
        val simpleType = clean.split(".").last()

        return when {
            // TextUnit (Text Size / Sp) -> NOT Dp!
            clean.contains("androidx.compose.ui.unit.TextUnit") -> PropertyKind.SpSlider to null

            // Dp (Elevations, paddings, sizes) -> DpSlider
            clean.contains("androidx.compose.ui.unit.Dp") -> PropertyKind.DpSlider to null

            // Single Color
            clean.contains("androidx.compose.ui.graphics.Color") || clean == "long" && paramName.contains("Color") ->
                PropertyKind.ColorPicker to null

            // Color Bundles (ButtonColors, CardColors, TextFieldColors, etc.)
            clean.endsWith("Colors") -> {
                val owner = simpleType.removeSuffix("Colors") + "Defaults"
                PropertyKind.ColorBundle to "$owner.${simpleType.replaceFirstChar { it.lowercase() }}()"
            }

            // Shapes
            clean.contains("androidx.compose.ui.graphics.Shape") -> PropertyKind.ShapePicker to null

            // Typography / TextStyle
            clean.contains("androidx.compose.ui.text.TextStyle") -> PropertyKind.TypographyPicker to null

            // Booleans
            clean == "boolean" || clean == "java.lang.Boolean" -> PropertyKind.Switch to null

            // Strings
            clean == "java.lang.String" || clean == "String" || paramName == "text" -> PropertyKind.Text to null

            // Primitives
            clean == "int" || clean == "java.lang.Integer" -> PropertyKind.Integer to null
            clean == "float" || clean == "java.lang.Float" || clean == "double" -> PropertyKind.FloatNumber to null

            // Modifier
            clean.contains("androidx.compose.ui.Modifier") -> PropertyKind.Modifier to null

            // Enums / Dropdown candidates
            clean.contains(".Alignment") || clean.contains(".Arrangement") || clean.contains("Overflow") ->
                PropertyKind.EnumDropdown to null

            else -> PropertyKind.Unknown to null
        }
    }

    fun cleanTypeDisplay(rawType: String): String {
        return rawType
            .replace("kotlin.jvm.functions.", "")
            .replace("kotlin.", "")
            .replace("java.lang.", "")
    }
}