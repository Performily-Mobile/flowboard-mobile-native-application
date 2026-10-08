package com.performily.flowboard.features.wellbeing.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.repository.ThresholdRepository
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import com.performily.flowboard.features.wellbeing.infrastructure.mapper.ThresholdMapper
import com.performily.flowboard.features.wellbeing.infrastructure.remote.OfficeService
import javax.inject.Inject

class ThresholdRepositoryImpl @Inject constructor(
    private val service: OfficeService
) : ThresholdRepository {

    override suspend fun getThresholds(officeId: Long): Result<List<MetricThreshold>> {
        return apiCall { service.getThresholds(officeId) }
            .mapCatching { dtos -> dtos.mapNotNull { ThresholdMapper.toDomain(it) } }
    }

    override suspend fun defineThreshold(
        officeId: Long,
        metricType: MetricType,
        ranges: List<ThresholdRange>
    ): Result<MetricThreshold> {
        return apiCall { service.defineThreshold(officeId, metricType.name, ThresholdMapper.toDefineRequest(ranges)) }
            .mapCatching { dto -> ThresholdMapper.toDomain(dto) ?: error("Métrica desconocida: ${dto.metricType}") }
    }
}
