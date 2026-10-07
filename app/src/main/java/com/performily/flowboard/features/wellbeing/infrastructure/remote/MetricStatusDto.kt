package com.performily.flowboard.features.wellbeing.infrastructure.remote

import java.math.BigDecimal

data class MetricStatusDto(
    val metricType: String,
    val unit: String?,
    val lastValue: BigDecimal?,
    val lastRecordedAt: String?,
    val upToDate: Boolean,
    val indicator: String?,
    val optimalMin: BigDecimal?,
    val optimalMax: BigDecimal?
)
