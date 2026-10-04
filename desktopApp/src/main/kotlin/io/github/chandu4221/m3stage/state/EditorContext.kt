package io.github.chandu4221.m3stage.state

import io.github.chandu4221.m3stage.model.Project
import io.github.chandu4221.m3stage.port.CodeGenerator
import io.github.chandu4221.m3stage.port.IdGenerator
import io.github.chandu4221.m3stage.port.ProjectRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class EditorContext(
    val idGenerator: IdGenerator,
    val repository: ProjectRepository,
    val codeGenerator: CodeGenerator, //
    val projectFlow: MutableStateFlow<Project?> = MutableStateFlow(null),
    val eventFlow: MutableSharedFlow<EditorEvent> = MutableSharedFlow()
)