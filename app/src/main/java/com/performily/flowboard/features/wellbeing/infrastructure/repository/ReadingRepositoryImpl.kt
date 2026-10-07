package com.performily.flowboard.features.wellbeing.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.domain.repository.ReadingRepository
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.infrastructure.mapper.ReadingHistoryMapper
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeService
import java.time.LocalDate
import javax.inject.Inject

class ReadingRepositoryImpl @Inject constructor(
    private val service: OfficeService
) : ReadingRepository {

    override suspend fun getReadingHistory(
        officeId: Long,
        metricType: MetricType,
        from: LocalDate,
        to: LocalDate
    ): Result<ReadingHistory> {
        return apiCall { service.getReadingHistory(officeId, metricType.name, from.toString(), to.toString()) }
            .mapCatching { dto -> ReadingHistoryMapper.toDomain(dto, metricType) }
    }
}
