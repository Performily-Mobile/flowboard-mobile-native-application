package com.performily.flowboard.features.wellbeing.domain.valueobject

import java.math.BigDecimal

/**
 * Rango de valores que corresponde a un indicador. Incluye el mínimo y excluye el
 * máximo: [min, max). Así dos rangos contiguos comparten el borde (0-600 y 600-1000).
 */
data class ThresholdRange(
    val indicator: HealthIndicator,
    val minValue: BigDecimal,
    val maxValue: BigDecimal
) {
    fun overlaps(other: ThresholdRange): Boolean = minValue < other.maxValue && other.minValue < maxValue
}
