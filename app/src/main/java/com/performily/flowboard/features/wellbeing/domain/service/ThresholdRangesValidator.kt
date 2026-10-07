package com.performily.flowboard.features.wellbeing.domain.service

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import java.math.BigDecimal

/**
 * Valida los rangos de una métrica con las mismas reglas que el backend (US49), para
 * mostrar el problema en la fila exacta antes de enviar (MA-74):
 * - Entre 2 y 4 rangos, uno por indicador.
 * - Cada rango con máximo mayor que el mínimo y dentro del rango físico de la métrica.
 * - Ordenados por mínimo, deben ser contiguos: sin superposiciones ni huecos.
 */
class ThresholdRangesValidator {

    fun validate(metricType: MetricType, ranges: List<ThresholdRange>): ThresholdValidation {
        val rowErrors = mutableMapOf<HealthIndicator, String>()
        var generalError: String? = null

        ranges.forEach { range ->
            when {
                range.maxValue <= range.minValue ->
                    rowErrors[range.indicator] = "El máximo debe ser mayor que el mínimo"
                !metricType.isWithinPhysicalRange(range.minValue) || !metricType.isWithinPhysicalRange(range.maxValue) ->
                    rowErrors[range.indicator] =
                        "Fuera del rango físico (${metricType.physicalMin.plain()} a ${metricType.physicalMax.plain()} ${metricType.unit})"
            }
        }

        if (ranges.size < MIN_RANGES) {
            generalError = "Define al menos $MIN_RANGES niveles."
        }

        if (rowErrors.isEmpty() && generalError == null) {
            val sorted = ranges.sortedBy { it.minValue }
            sorted.zipWithNext().forEach { (current, next) ->
                when {
                    current.overlaps(next) -> {
                        rowErrors.putIfAbsent(next.indicator,
                            "Se superpone con ${current.indicator.labelEs()} (${next.minValue.plain()}–${current.maxValue.plain()})")
                        generalError = "Los rangos se superponen. Corrige los valores para guardar."
                    }
                    current.maxValue < next.minValue -> {
                        rowErrors.putIfAbsent(next.indicator,
                            "Falta cubrir de ${current.maxValue.plain()} a ${next.minValue.plain()}")
                        if (generalError == null) {
                            generalError = "Hay vacíos entre los rangos. Completa los intervalos faltantes."
                        }
                    }
                }
            }
        }
        return ThresholdValidation(rowErrors, generalError)
    }

    private fun BigDecimal.plain(): String = stripTrailingZeros().toPlainString()

    private fun HealthIndicator.labelEs(): String = when (this) {
        HealthIndicator.OPTIMAL -> "Óptimo"
        HealthIndicator.ACCEPTABLE -> "Aceptable"
        HealthIndicator.POOR -> "Deficiente"
        HealthIndicator.HAZARDOUS -> "Peligro"
    }

    companion object {
        const val MIN_RANGES = 2
    }
}

/**
 * @property rowErrors error de cada fila (indicador) que tiene un problema
 * @property generalError mensaje general para el aviso de la pantalla
 */
data class ThresholdValidation(
    val rowErrors: Map<HealthIndicator, String>,
    val generalError: String?
) {
    val isValid: Boolean get() = rowErrors.isEmpty() && generalError == null
}
