package com.performily.flowboard.features.benefits.infrastructure.mapper

import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.entity.VacationMovement
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation
import com.performily.flowboard.features.benefits.infrastructure.remote.AdjustVacationBalanceRequestDto
import com.performily.flowboard.features.benefits.infrastructure.remote.VacationBalanceDto
import java.math.BigDecimal
import java.time.LocalDateTime

object VacationBalanceMapper {

    fun toDomain(dto: VacationBalanceDto): VacationBalance = VacationBalance(
        employeeId = dto.employeeId,
        employeeName = dto.employeeName?.takeIf { it.isNotBlank() },
        areaName = dto.areaName?.takeIf { it.isNotBlank() },
        accruedDays = dto.accruedDays,
        usedDays = dto.usedDays,
        availableDays = dto.availableDays,
        lastAccrualDate = BenefitsEnumMapper.date(dto.lastAccrualDate),
        movements = dto.movements.orEmpty()
            .map { movement ->
                VacationMovement(
                    id = movement.id,
                    type = BenefitsEnumMapper.movementType(movement.type),
                    days = movement.days,
                    reason = movement.reason?.takeIf { it.isNotBlank() },
                    authorName = movement.authorName?.takeIf { it.isNotBlank() },
                    requestId = movement.requestId,
                    occurredAt = BenefitsEnumMapper.dateTime(movement.occurredAt) ?: LocalDateTime.of(1970, 1, 1, 0, 0)
                )
            }
            .sortedByDescending { it.occurredAt }
    )

    fun toAdjustRequest(operation: AdjustmentOperation, days: BigDecimal, reason: String, authorId: Long) =
        AdjustVacationBalanceRequestDto(operation = operation.name, days = days, reason = reason, authorId = authorId)
}
