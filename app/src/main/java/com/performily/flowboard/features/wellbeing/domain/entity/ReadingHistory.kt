package com.performily.flowboard.features.wellbeing.domain.entity

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Histórico de una métrica en un rango de fechas con su resumen.
 *
 * @property daysAboveAcceptable días cuyo promedio quedó en Deficiente o Peligro
 */
data class ReadingHistory(
    val officeId: Long,
    val metricType: MetricType,
    val from: LocalDate,
    val to: LocalDate,
    val minimum: BigDecimal?,
    val maximum: BigDecimal?,
    val average: BigDecimal?,
    val daysAboveAcceptable: Int,
    val dailyAverages: List<DailyAverage>,
    val readingsCount: Int,
    val message: String?
) {
    val isEmpty: Boolean get() = readingsCount == 0

    /** Tres días o más sobre el nivel aceptable se consideran un problema persistente. */
    val isPersistentProblem: Boolean get() = daysAboveAcceptable >= PERSISTENT_DAYS

    companion object {
        const val PERSISTENT_DAYS = 3
    }
}

data class DailyAverage(
    val date: LocalDate,
    val average: BigDecimal,
    val indicator: HealthIndicator?
)
