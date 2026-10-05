package com.performily.flowboard.features.workspace.infrastructure.mapper

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.infrastructure.remote.CreatePositionRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.PositionDto

object PositionMapper {

    fun toDomain(dto: PositionDto): Position {
        return Position(
            id = dto.id,
            title = dto.title,
            areaId = dto.areaId,
            areaName = dto.areaName.orEmpty(),
            referenceSalary = Money(
                amount = dto.referenceSalaryAmount,
                currency = dto.referenceSalaryCurrency ?: Money.DEFAULT_CURRENCY
            ),
            active = dto.active
        )
    }

    fun toCreateRequest(title: String, areaId: Long, referenceSalary: Money): CreatePositionRequestDto {
        return CreatePositionRequestDto(
            title = title,
            areaId = areaId,
            referenceSalaryAmount = referenceSalary.amount,
            referenceSalaryCurrency = referenceSalary.currency
        )
    }
}
