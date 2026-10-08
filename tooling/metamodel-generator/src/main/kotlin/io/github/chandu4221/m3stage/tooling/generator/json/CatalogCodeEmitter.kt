package io.github.chandu4221.m3stage.tooling.generator.json

import io.github.chandu4221.m3stage.tooling.generator.model.ComponentOriginKind
import io.github.chandu4221.m3stage.tooling.generator.model.RawM3Component
import java.io.File

object CatalogCodeEmitter {

    /**
     * Emits a comprehensive Markdown inventory report of all extracted Material 3 components,
     * including overloads, defaults members, receivers, properties, callbacks, and named slots.
     */
    fun emitMarkdownReport(components: List<RawM3Component>, outputFile: File) {
        val sb = StringBuilder()
        sb.appendLine("# Extracted Material 3 Catalog Inventory")
        sb.appendLine()
        val totalOverloads = components.sumOf { it.overloads.size }
        val topLevel = components.filter { it.originKind == ComponentOriginKind.Component }
        val defaultsMembers = components.filter { it.originKind == ComponentOriginKind.DefaultsMember }

        sb.appendLine("- Total Components: **${components.size}** (${topLevel.size} top-level, ${defaultsMembers.size} defaults members)")
        sb.appendLine("- Total Signatures / Overloads: **$totalOverloads**")
        sb.appendLine()

        sb.appendLine("## 1. Top-Level Components")
        sb.appendLine()
        sb.appendLine("| Component | Overloads | Receiver | Container | Sample Properties | Composable Slots | Callbacks |")
        sb.appendLine("|---|---|---|---|---|---|---|")

        for (comp in topLevel) {
            val primary = comp.overloads.maxByOrNull { it.parameters.size + it.slots.size } ?: comp.overloads.first()
            val receiverStr = primary.receiver?.let { "`$it`" } ?: "-"
            val propsStr = if (primary.parameters.isEmpty()) "-" else "${primary.parameters.size} props"
            val slotsStr = if (primary.slots.isEmpty()) "-" else primary.slots.joinToString(", ") {
                "${it.name}${if (it.scope != null) " (${it.scope})" else ""}"
            }
            val callbacksStr = if (primary.callbacks.isEmpty()) "-" else primary.callbacks.joinToString(", ") { it.name }

            sb.appendLine("| `${comp.name}` | ${comp.overloads.size} | $receiverStr | ${if (primary.isContainer) "✅" else "❌"} | $propsStr | $slotsStr | $callbacksStr |")
        }

        if (defaultsMembers.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("## 2. Defaults Object Members (e.g. SliderDefaults.Thumb)")
            sb.appendLine()
            sb.appendLine("| Defaults Member | Owner | Overloads | Sample Properties | Slots |")
            sb.appendLine("|---|---|---|---|---|")
            for (comp in defaultsMembers) {
                val primary = comp.overloads.first()
                val slotsStr = if (primary.slots.isEmpty()) "-" else primary.slots.joinToString(", ") { it.name }
                sb.appendLine("| `${comp.name}` | `${comp.owner ?: "Defaults"}` | ${comp.overloads.size} | ${primary.parameters.size} props | $slotsStr |")
            }
        }

        outputFile.parentFile?.mkdirs()
        outputFile.writeText(sb.toString())
    }
}