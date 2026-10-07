package com.performily.flowboard.features.wellbeing.domain.repository

import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange

interface ThresholdRepository {
    suspend fun getThresholds(officeId: Long): Result<List<MetricThreshold>>
    suspend fun defineThreshold(officeId: Long, metricType: MetricType, ranges: List<ThresholdRange>): Result<MetricThreshold>
}
