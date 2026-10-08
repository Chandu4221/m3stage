package io.github.chandu4221.m3stage.tooling.generator.json

import io.github.chandu4221.m3stage.tooling.generator.model.RawM3Component
import kotlinx.serialization.json.Json
import java.io.File

object MetamodelJsonEmitter {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Serializes components to JSON string.
     */
    fun emitString(components: List<RawM3Component>): String {
        return json.encodeToString(components)
    }

    /**
     * Writes components JSON manifest to target file.
     */
    fun emitToFile(components: List<RawM3Component>, outputFile: File) {
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(emitString(components))
    }
}