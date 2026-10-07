package com.performily.flowboard.features.wellbeing.infrastructure.remote

data class MetricThresholdDto(
    val id: Long,
    val officeId: Long,
    val metricType: String,
    val unit: String?,
    val ranges: List<ThresholdRangeDto>?
)
