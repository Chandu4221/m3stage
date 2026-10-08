package io.github.chandu4221.m3stage.tooling.generator

import io.github.chandu4221.m3stage.tooling.generator.json.CatalogCodeEmitter
import io.github.chandu4221.m3stage.tooling.generator.json.MetamodelJsonEmitter
import io.github.chandu4221.m3stage.tooling.generator.model.ComponentOriginKind
import io.github.chandu4221.m3stage.tooling.generator.parser.MetalavaParser
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

fun main(args: Array<String>) {
    println("==================================================")
    println("   M3Stage Material 3 Metamodel Extractor CLI    ")
    println("==================================================")

    val rawUrl = "https://raw.githubusercontent.com/androidx/androidx/refs/heads/androidx-main/compose/material3/material3/api/current.txt"

    println("Fetching upstream Metalava signature from:")
    println(rawUrl)

    val content: String = try {
        fetchUpstreamSignature(rawUrl)
    } catch (e: Exception) {
        println("⚠️ Network fetch failed (${e.message}). Falling back to local cache if available.")
        val localCache = File("build/cache/current.txt")
        if (localCache.exists()) {
            localCache.readText()
        } else {
            throw IllegalStateException("Could not fetch remote signature and no local cache found.", e)
        }
    }

    println("Parsing Metalava signatures with multi-overload and AST extractor...")
    val parser = MetalavaParser()
    val components = parser.parse(content)

    val totalOverloads = components.sumOf { it.overloads.size }
    val topLevel = components.filter { it.originKind == ComponentOriginKind.Component }
    val defaults = components.filter { it.originKind == ComponentOriginKind.DefaultsMember }

    println("Extracted ${components.size} components (${topLevel.size} top-level, ${defaults.size} defaults members) with $totalOverloads distinct overloads!")

    val outDir = File("build/generated/m3-schema")
    outDir.mkdirs()

    val jsonFile = File(outDir, "m3-components.json")
    val mdFile = File(outDir, "M3_CATALOG_INVENTORY.md")

    println("Emitting JSON schema to: ${jsonFile.absolutePath}")
    MetamodelJsonEmitter.emitToFile(components, jsonFile)

    println("Emitting Markdown inventory report to: ${mdFile.absolutePath}")
    CatalogCodeEmitter.emitMarkdownReport(components, mdFile)

    println()
    println("Sample Top-Level Components:")
    topLevel.take(8).forEach { comp ->
        val primary = comp.overloads.maxByOrNull { it.parameters.size + it.slots.size } ?: comp.overloads.first()
        println(" - ${comp.name} [${comp.overloads.size} overloads] (props: ${primary.parameters.size}, slots: ${primary.slots.size}, callbacks: ${primary.callbacks.size}, receiver: ${primary.receiver ?: "none"})")
    }

    println()
    println("Sample Defaults Members:")
    defaults.take(4).forEach { comp ->
        println(" - ${comp.name} (owner: ${comp.owner})")
    }

    println()
    println("Extractor run completed successfully!")
}

private fun fetchUpstreamSignature(url: String): String {
    val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() != 200) {
        throw IllegalStateException("HTTP ${response.statusCode()} while fetching $url")
    }
    return response.body()
}