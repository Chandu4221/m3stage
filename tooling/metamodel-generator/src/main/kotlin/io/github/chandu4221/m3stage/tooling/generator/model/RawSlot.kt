package io.github.chandu4221.m3stage.tooling.generator.model

import kotlinx.serialization.Serializable

@Serializable
data class RawSlot(
    val name: String,
    val isRequired: Boolean,
    val isNullable: Boolean,
    val scope: String? = null, // e.g. "RowScope", "ColumnScope", "AppBarColumnScope"
    val args: List<String> = emptyList() // e.g. ["PaddingValues"] for Scaffold.content
)