package com.performily.flowboard.features.workspace.infrastructure.remote

import java.math.BigDecimal

data class CreatePositionRequestDto(
    val title: String,
    val areaId: Long,
    val referenceSalaryAmount: BigDecimal,
    val referenceSalaryCurrency: String
)
