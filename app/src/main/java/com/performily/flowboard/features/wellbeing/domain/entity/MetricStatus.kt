package com.performily.flowboard.features.wellbeing.domain.entity

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Condición actual de una métrica en un espacio.
 *
 * Si la última lectura es más antigua que la ventana de vigencia, upToDate es false
 * e indicator es null: la información no está actualizada y no se emite indicador.
 */
data class MetricStatus(
    val metricType: MetricType,
    val lastValue: BigDecimal?,
    val lastRecordedAt: LocalDateTime?,
    val upToDate: Boolean,
    val indicator: HealthIndicator?,
    val optimalMin: BigDecimal?,
    val optimalMax: BigDecimal?
) {
    val hasReadings: Boolean get() = lastValue != null
    val hasOptimalRange: Boolean get() = optimalMin != null && optimalMax != null
}
