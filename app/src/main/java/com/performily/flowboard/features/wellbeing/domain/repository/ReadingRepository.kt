package com.performily.flowboard.features.wellbeing.domain.repository

import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.time.LocalDate

interface ReadingRepository {
    suspend fun getReadingHistory(officeId: Long, metricType: MetricType, from: LocalDate, to: LocalDate): Result<ReadingHistory>
}
