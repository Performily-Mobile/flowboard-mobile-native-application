package com.performily.flowboard.features.workspace.infrastructure.mapper

import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.valueobject.AssignmentChangeType
import com.performily.flowboard.features.workspace.infrastructure.remote.AssignJobRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.JobAssignmentDto
import java.time.LocalDate

object JobAssignmentMapper {

    fun toDomain(dto: JobAssignmentDto): JobAssignment {
        return JobAssignment(
            id = dto.id,
            areaId = dto.areaId,
            areaName = dto.areaName.orEmpty(),
            positionId = dto.positionId,
            positionTitle = dto.positionTitle.orEmpty(),
            changeType = AssignmentChangeType.valueOf(dto.changeType),
            startDate = LocalDate.parse(dto.startDate),
            endDate = dto.endDate?.let(LocalDate::parse),
            current = dto.current
        )
    }

    fun toAssignRequest(areaId: Long, positionId: Long, effectiveDate: LocalDate): AssignJobRequestDto =
        AssignJobRequestDto(
            areaId = areaId,
            positionId = positionId,
            effectiveDate = effectiveDate.toString()
        )
}
