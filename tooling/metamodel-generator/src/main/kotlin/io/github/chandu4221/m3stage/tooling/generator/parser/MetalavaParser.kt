package io.github.chandu4221.m3stage.tooling.generator.parser

import io.github.chandu4221.m3stage.tooling.generator.model.*

class MetalavaParser {

    fun parse(content: String): List<RawM3Component> {
        val lines = content.lines()
        val rawComponents = mutableListOf<ParsedFunction>()

        var currentPackage = ""
        var currentClass = ""

        for (line in lines) {
            val trimmed = line.trim()

            // Package tracking (Issue 8: Subpackages included)
            if (trimmed.startsWith("package ") && trimmed.endsWith("{")) {
                currentPackage = trimmed.removePrefix("package ").removeSuffix("{").trim()
                continue
            }

            // Issue 8: Keep all M3 public packages, exclude internal/tokens
            if (!currentPackage.startsWith("androidx.compose.material3") ||
                currentPackage.contains(".internal") ||
                currentPackage.contains(".tokens")
            ) {
                continue
            }

            // Class tracking (Issue 2: Defaults member vs Component)
            if (trimmed.startsWith("public final class ") || trimmed.startsWith("public class ") || trimmed.startsWith("public interface ")) {
                val classTokens = trimmed.split(" ")
                val classIdx = classTokens.indexOfFirst { it == "class" || it == "interface" }
                if (classIdx != -1 && classIdx + 1 < classTokens.size) {
                    currentClass = classTokens[classIdx + 1].removeSuffix("{").trim()
                }
                continue
            }

            // Composable method tracking
            if (trimmed.startsWith("method ") &&
                trimmed.contains("@androidx.compose.runtime.Composable") &&
                trimmed.contains("@KotlinOnly")
            ) {
                parseMethodLine(trimmed, currentPackage, currentClass)?.let { parsed ->
                    rawComponents.add(parsed)
                }
            }
        }

        // Issue 1: Group by component name and PRESERVE ALL OVERLOADS!
        return rawComponents
            .groupBy { it.name }
            .map { (name, functions) ->
                val first = functions.first()
                val isDefaultsMember = first.enclosingClass.endsWith("Defaults")
                val originKind = if (isDefaultsMember) ComponentOriginKind.DefaultsMember else ComponentOriginKind.Component
                val owner = if (isDefaultsMember) first.enclosingClass else null

                RawM3Component(
                    name = name,
                    packageName = first.packageName,
                    originKind = originKind,
                    owner = owner,
                    overloads = functions.map { it.overload }
                )
            }
            .sortedBy { it.name }
    }

    private data class ParsedFunction(
        val name: String,
        val packageName: String,
        val enclosingClass: String,
        val overload: ComponentOverload
    )

    private fun parseMethodLine(line: String, packageName: String, enclosingClass: String): ParsedFunction? {
        val isDeprecated = line.contains("@Deprecated")

        // Issue 7: Opt-in annotations extraction
        val optInList = mutableListOf<String>()
        val optInRegex = Regex("@([a-zA-Z0-9_.]*Experimental[a-zA-Z0-9_.]*)")
        optInRegex.findAll(line).forEach { match ->
            val simpleName = match.groupValues[1].split(".").last()
            optInList.add(simpleName)
        }

        val openParenIdx = line.indexOf('(')
        val closeParenIdx = line.lastIndexOf(')')
        if (openParenIdx == -1 || closeParenIdx == -1 || closeParenIdx <= openParenIdx) return null

        val methodPrefix = line.substring(0, openParenIdx).trim()
        val methodName = methodPrefix.split(" ").lastOrNull() ?: return null

        // Ignore internal or synthetic functions
        if (methodName.contains("$") || methodName.contains("-")) return null
        if (!methodName.first().isUpperCase()) return null

        val paramString = line.substring(openParenIdx + 1, closeParenIdx).trim()

        // Issue 4: Depth-aware splitting (handles @FloatRange(from=0.0, to=1.0) without breaking)
        val rawParams = splitParameters(paramString)

        val parameters = mutableListOf<RawParameter>()
        val callbacks = mutableListOf<RawCallback>()
        val slots = mutableListOf<RawSlot>()
        var receiver: String? = null
        var hasModifier = false

        for (rawParam in rawParams) {
            if (rawParam.isBlank()) continue

            val (name, type, isOptional) = parseParameter(rawParam)

            // Issue 3: Extract extension receivers
            if (TypeClassifier.isExtensionReceiver(name, type) && receiver == null) {
                receiver = TypeClassifier.extractReceiverSimpleName(type)
            } else if (TypeClassifier.isModifier(type, name)) {
                hasModifier = true
            } else if (TypeClassifier.isComposableSlot(type, name)) {
                // Issue 5: Composable slots
                slots.add(TypeClassifier.extractSlot(name, type, isOptional))
            } else if (TypeClassifier.isCallback(name, type)) {
                // Issue 5: Callbacks
                callbacks.add(TypeClassifier.extractCallback(name, type, isOptional))
            } else {
                // Issue 6: Classified property
                val (kind, defaultsFactory) = TypeClassifier.classifyProperty(name, type)
                parameters.add(
                    RawParameter(
                        name = name,
                        rawType = type,
                        isOptional = isOptional,
                        isNullable = type.contains("?"),
                        kind = kind,
                        defaultsFactory = defaultsFactory
                    )
                )
            }
        }

        val isContainer = slots.any { it.name == "content" }

        val signatureId = "$methodName(${parameters.size}p,${slots.size}s,${callbacks.size}c)"

        val overload = ComponentOverload(
            signatureId = signatureId,
            isDeprecated = isDeprecated,
            optIn = optInList.distinct(),
            receiver = receiver,
            parameters = parameters,
            callbacks = callbacks,
            slots = slots,
            hasModifier = hasModifier,
            isContainer = isContainer
        )

        return ParsedFunction(
            name = methodName,
            packageName = packageName,
            enclosingClass = enclosingClass,
            overload = overload
        )
    }

    /**
     * Issue 4: Splits comma-separated parameters respecting <...>, (...), and annotations.
     */
    private fun splitParameters(paramString: String): List<String> {
        if (paramString.isBlank()) return emptyList()

        val params = mutableListOf<String>()
        var angleDepth = 0
        var parenDepth = 0
        var current = StringBuilder()

        for (char in paramString) {
            when (char) {
                '<' -> {
                    angleDepth++
                    current.append(char)
                }
                '>' -> {
                    angleDepth--
                    current.append(char)
                }
                '(' -> {
                    parenDepth++
                    current.append(char)
                }
                ')' -> {
                    parenDepth--
                    current.append(char)
                }
                ',' -> {
                    if (angleDepth == 0 && parenDepth == 0) {
                        params.add(current.toString().trim())
                        current = StringBuilder()
                    } else {
                        current.append(char)
                    }
                }
                else -> current.append(char)
            }
        }
        if (current.isNotBlank()) {
            params.add(current.toString().trim())
        }

        return params
    }

    /**
     * Issue 4: Strips annotations from parameter declaration before extracting type and name.
     */
    private fun parseParameter(chunk: String): Triple<String, String, Boolean> {
        var clean = chunk.trim()
        val isOptional = clean.startsWith("optional ")
        if (isOptional) {
            clean = clean.removePrefix("optional ").trim()
        }

        // Strip leading annotations like @FloatRange(...) or @androidx.annotation.IntRange(...)
        clean = clean.replace(Regex("@[a-zA-Z0-9_.]+(\\([^)]*\\))?\\s+"), "").trim()

        val lastSpace = clean.lastIndexOf(' ')
        return if (lastSpace != -1) {
            val type = clean.substring(0, lastSpace).trim()
            val name = clean.substring(lastSpace + 1).trim()
            Triple(name, type, isOptional)
        } else {
            Triple(clean, clean, isOptional)
        }
    }
}