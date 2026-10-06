package com.performily.flowboard.features.workspace.infrastructure.remote

import java.math.BigDecimal

data class UpdatePositionReferenceSalaryRequestDto(
    val referenceSalaryAmount: BigDecimal,
    val referenceSalaryCurrency: String
)
