package io.github.chandu4221.m3stage.adapter

import io.github.chandu4221.m3stage.model.NodeId
import io.github.chandu4221.m3stage.model.ProjectId
import io.github.chandu4221.m3stage.model.ScreenId
import io.github.chandu4221.m3stage.port.IdGenerator
import java.util.*

class UuidIdGenerator : IdGenerator {
    override fun nextProjectId(): ProjectId = ProjectId(UUID.randomUUID().toString())

    override fun nextScreenId(): ScreenId = ScreenId(UUID.randomUUID().toString())

    override fun nextNodeId(): NodeId = NodeId(UUID.randomUUID().toString())
}