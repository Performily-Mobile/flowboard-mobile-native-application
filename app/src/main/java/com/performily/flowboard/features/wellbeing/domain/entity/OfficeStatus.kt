package com.performily.flowboard.features.wellbeing.domain.entity

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import java.time.LocalDateTime

/**
 * Estado ambiental de un espacio: sus métricas y sus dispositivos vinculados.
 *
 * @property overallIndicator el peor indicador de las métricas vigentes; pinta la tarjeta del espacio
 * @property upToDate true si al menos una métrica tiene lecturas recientes
 */
data class OfficeStatus(
    val office: Office,
    val upToDate: Boolean,
    val overallIndicator: HealthIndicator?,
    val lastReadingAt: LocalDateTime?,
    val metrics: List<MetricStatus>,
    val devices: List<Device>
) {
    val hasReadings: Boolean get() = lastReadingAt != null

    /** La métrica vigente en peor estado, para el aviso de la pantalla de indicadores. */
    val worstMetric: MetricStatus?
        get() = metrics.filter { it.upToDate && it.indicator != null }.maxByOrNull { it.indicator!!.severity }
}
