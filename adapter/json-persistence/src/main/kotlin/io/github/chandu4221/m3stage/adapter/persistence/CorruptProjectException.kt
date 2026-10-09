package io.github.chandu4221.m3stage.adapter.persistence

import java.io.File

class CorruptProjectException(
    val file: File,
    val backupFile: File?,
    cause: Throwable
) : RuntimeException(
    "Project file '${file.name}' is corrupt: ${cause.message}. Backup saved to: ${backupFile?.name}",
    cause
)