package com.performily.flowboard.features.wellbeing.infrastructure.remote

/** Respuesta de GET /offices y GET /offices/{id}/status (OfficeStatusResource del backend). */
data class OfficeStatusDto(
    val id: Long,
    val name: String,
    val area: String?,
    val address: String,
    val floor: String,
    val reference: String?,
    val active: Boolean,
    val upToDate: Boolean,
    val overallIndicator: String?,
    val lastReadingAt: String?,
    val metrics: List<MetricStatusDto>?,
    val devices: List<DeviceDto>?
)
