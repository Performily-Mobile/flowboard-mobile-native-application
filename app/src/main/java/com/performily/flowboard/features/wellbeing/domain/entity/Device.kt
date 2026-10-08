package com.performily.flowboard.features.wellbeing.domain.entity

import com.performily.flowboard.features.wellbeing.domain.valueobject.DeviceStatus
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.time.LocalDateTime

/**
 * Dispositivo de medición del inventario.
 *
 * @property lastReadingAt última lectura enviada; solo viene en los dispositivos de un espacio
 */
data class Device(
    val id: Long,
    val code: String,
    val supportedMetrics: List<MetricType>,
    val status: DeviceStatus,
    val officeId: Long?,
    val lastReadingAt: LocalDateTime?
) {
    /** "Temperatura · Aire" */
    val metricsSummary: String
        get() = MetricType.entries.filter { it in supportedMetrics }.joinToString(" · ") { it.shortLabel }
}
