package com.performily.flowboard.features.benefits.infrastructure.remote

import java.math.BigDecimal

data class VacationBalanceDto(
    val employeeId: Long,
    val employeeName: String?,
    val areaName: String?,
    val accruedDays: BigDecimal,
    val usedDays: BigDecimal,
    val availableDays: BigDecimal,
    val lastAccrualDate: String?,
    val movements: List<VacationMovementDto>?
)

data class VacationMovementDto(
    val id: Long,
    val type: String,
    val days: BigDecimal,
    val reason: String?,
    val authorId: Long?,
    val authorName: String?,
    val requestId: Long?,
    val occurredAt: String
)

data class AdjustVacationBalanceRequestDto(
    val operation: String,
    val days: BigDecimal,
    val reason: String,
    val authorId: Long
)
