package com.performily.flowboard.features.workspace.infrastructure.mapper

import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.infrastructure.remote.AreaDto
import com.performily.flowboard.features.workspace.infrastructure.remote.CreateAreaRequestDto

object AreaMapper {

    fun toDomain(dto: AreaDto): Area {
        return Area(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            active = dto.active,
            activeEmployees = dto.activeEmployees
        )
    }

    fun toCreateRequest(name: String, description: String?): CreateAreaRequestDto =
        CreateAreaRequestDto(name = name, description = description)
}
