package com.performily.flowboard.features.wellbeing.infrastructure.remote

/** DeviceResource del backend. */
data class DeviceDto(
    val id: Long,
    val code: String,
    val supportedMetrics: List<String>?,
    val status: String,
    val officeId: Long?,
    val lastReadingAt: String?
)
