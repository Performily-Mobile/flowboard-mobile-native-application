package com.performily.flowboard.features.wellbeing.infrastructure.remote

import java.math.BigDecimal

/** Respuesta de GET /offices/{id}/readings (ReadingHistoryResource del backend). */
data class ReadingHistoryDto(
    val officeId: Long,
    val metricType: String,
    val unit: String?,
    val from: String,
    val to: String,
    val minimum: BigDecimal?,
    val maximum: BigDecimal?,
    val average: BigDecimal?,
    val daysAboveAcceptable: Int,
    val dailyAverages: List<DailyAverageDto>?,
    val readings: List<ReadingDto>?,
    val message: String?
)

data class DailyAverageDto(
    val date: String,
    val average: BigDecimal,
    val indicator: String?
)

data class ReadingDto(
    val id: Long,
    val officeId: Long,
    val deviceId: Long,
    val metricType: String,
    val value: BigDecimal,
    val unit: String?,
    val recordedAt: String,
    val indicator: String?
)
