package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.domain.repository.ReadingRepository
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.time.LocalDate
import javax.inject.Inject

/** MA-75 · Histórico de una métrica en un rango de fechas (US51). */
class GetReadingHistoryUseCase @Inject constructor(private val repository: ReadingRepository) {
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
        return repository.getReadingHistory(officeId, metricType, from, to)
    }
}
