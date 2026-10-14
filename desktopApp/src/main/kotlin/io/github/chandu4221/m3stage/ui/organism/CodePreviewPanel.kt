package io.github.chandu4221.m3stage.ui.organism

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chandu4221.m3stage.ui.atom.ToolIconButton

/**
 * Dumb Organism: Collapsible bottom panel showing live generated Jetpack Compose code.
 */
@Composable
fun CodePreviewPanel(
    fileName: String,
    code: String,
    isGenerating: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            kotlinx.coroutines.delay(2000)
            copied = false
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Tab & Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isGenerating) {
                            Text(
                                text = "Generating...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Right: Actions (Copy & Close)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(code))
                                copied = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (copied) "Copied!" else "Copy",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        ToolIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Close preview",
                            onClick = onClose,
                            tooltip = "Close Code Preview"
                        )
                    }
                }
            }

            // Code Content Area with Line Numbers & Syntax Highlighting
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(8.dp)
            ) {
                if (isGenerating && code.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()

                    SelectionContainer {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(verticalScroll)
                                .horizontalScroll(horizontalScroll)
                        ) {
                            val lines = code.lines()
                            val lineNumbersText = (1..lines.size).joinToString("\n")

                            // Line numbers gutter
                            Text(
                                text = lineNumbersText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            // Highlighted code
                            val colorScheme = MaterialTheme.colorScheme
                            val highlighted = remember(code, colorScheme) {
                                highlightKotlinCode(code, colorScheme)
                            }
                            Text(
                                text = highlighted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lightweight regex-based syntax highlighter for Kotlin/Compose code.
 * Highlights keywords, strings, annotations, and comments using MaterialTheme colorScheme tokens.
 */
private fun highlightKotlinCode(raw: String, colorScheme: ColorScheme): AnnotatedString {
    val keywords = setOf(
        "package", "import", "class", "fun", "val", "var", "if", "else", "when",
        "for", "while", "return", "true", "false", "null", "is", "in", "object",
        "data", "sealed", "interface", "private", "public", "protected", "internal",
        "override", "by"
    )

    val keywordColor = colorScheme.primary
    val annotationColor = colorScheme.tertiary
    val stringColor = colorScheme.secondary
    val commentColor = colorScheme.outline
    val defaultColor = colorScheme.onSurface

    val builder = AnnotatedString.Builder()

    val stringOrCommentPattern = Regex("\"(\\\\.|[^\"])*\"|//.*")
    val annotationPattern = Regex("@[a-zA-Z0-9_]+")
    val wordPattern = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")

    // Simple line-by-line / token scanner
    raw.lines().forEachIndexed { lineIdx, line ->
        if (lineIdx > 0) builder.append("\n")

        var cursor = 0
        while (cursor < line.length) {
            // Check comment: //
            val commentIdx = line.indexOf("//", cursor)
            if (commentIdx == cursor) {
                builder.pushStyle(SpanStyle(color = commentColor))
                builder.append(line.substring(cursor))
                builder.pop()
                cursor = line.length
                break
            }

            // Check String literal: "..."
            if (line[cursor] == '"') {
                val endQuote = line.indexOf('"', cursor + 1)
                val strLiteral = if (endQuote != -1) line.substring(cursor, endQuote + 1) else line.substring(cursor)
                builder.pushStyle(SpanStyle(color = stringColor))
                builder.append(strLiteral)
                builder.pop()
                cursor += strLiteral.length
                continue
            }

            // Check Annotation: @...
            if (line[cursor] == '@') {
                val match = annotationPattern.find(line, cursor)
                if (match != null && match.range.first == cursor) {
                    builder.pushStyle(SpanStyle(color = annotationColor, fontWeight = FontWeight.Bold))
                    builder.append(match.value)
                    builder.pop()
                    cursor += match.value.length
                    continue
                }
            }

            // Check Identifiers / Keywords
            val char = line[cursor]
            if (char.isLetter() || char == '_') {
                val start = cursor
                while (cursor < line.length && (line[cursor].isLetterOrDigit() || line[cursor] == '_')) {
                    cursor++
                }
                val word = line.substring(start, cursor)
                if (word in keywords) {
                    builder.pushStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold))
                    builder.append(word)
                    builder.pop()
                } else {
                    builder.pushStyle(SpanStyle(color = defaultColor))
                    builder.append(word)
                    builder.pop()
                }
                continue
            }

            // Other punctuation / whitespace
            builder.pushStyle(SpanStyle(color = defaultColor))
            builder.append(char.toString())
            builder.pop()
            cursor++
        }
    }

    return builder.toAnnotatedString()
}
