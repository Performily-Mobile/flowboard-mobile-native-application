package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.domain.repository.ReadingRepository
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

/** MA-75 - History of a metric in a date range (US51). */
class GetReadingHistoryUseCase @Inject constructor(private val repository: ReadingRepository) {

    /**
     * Loads the history after validating the range.
     *
     * The backend rejects future dates according to its own clock, so the end date sent is never
     * later than today in UTC.
     *
     * @param officeId office to query
     * @param metricType metric to query
     * @param from first day of the range
     * @param to last day of the range
     * @param today current device date, injectable for tests
     * @return the history, or a failure when the range is invalid
     */
    suspend operator fun invoke(
        officeId: Long,
        metricType: MetricType,
        from: LocalDate,
        to: LocalDate,
        today: LocalDate = LocalDate.now()
    ): Result<ReadingHistory> {
        if (from.isAfter(to)) {
            return Result.failure(IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha final."))
        }
        if (to.isAfter(today)) {
            return Result.failure(IllegalArgumentException("El rango no puede incluir fechas futuras."))
        }
        val safeTo = minOf(to, LocalDate.now(ZoneOffset.UTC))
        return repository.getReadingHistory(officeId, metricType, minOf(from, safeTo), safeTo)
    }
}
