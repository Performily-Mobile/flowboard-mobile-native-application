package com.performily.flowboard.features.workspace.infrastructure.remote

import java.math.BigDecimal

data class PositionDto(
    val id: Long,
    val title: String,
    val areaId: Long,
    val areaName: String?,
    val referenceSalaryAmount: BigDecimal,
    val referenceSalaryCurrency: String?,
    val active: Boolean
)
