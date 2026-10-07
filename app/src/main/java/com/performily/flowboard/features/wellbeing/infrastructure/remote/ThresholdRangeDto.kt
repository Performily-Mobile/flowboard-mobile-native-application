package com.performily.flowboard.features.wellbeing.infrastructure.remote

import java.math.BigDecimal

data class ThresholdRangeDto(
    val indicator: String,
    val minValue: BigDecimal,
    val maxValue: BigDecimal
)
