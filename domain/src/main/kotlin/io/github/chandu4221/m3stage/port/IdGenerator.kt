package io.github.chandu4221.m3stage.port

import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.ScreenId

interface IdGenerator {
    fun nextProjectId(): ProjectId
    fun nextScreenId(): ScreenId
    fun nextNodeId(): NodeId
}