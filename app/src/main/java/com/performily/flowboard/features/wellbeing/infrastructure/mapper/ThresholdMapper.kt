package com.performily.flowboard.features.wellbeing.infrastructure.mapper

import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import com.performily.flowboard.features.wellbeing.infrastructure.remote.DefineMetricThresholdRequestDto
import com.performily.flowboard.features.wellbeing.infrastructure.remote.MetricThresholdDto
import com.performily.flowboard.features.wellbeing.infrastructure.remote.ThresholdRangeDto

object ThresholdMapper {

    fun toDomain(dto: MetricThresholdDto): MetricThreshold? {
        val metricType = WellbeingEnumMapper.metricType(dto.metricType) ?: return null
        return MetricThreshold(
            id = dto.id,
            officeId = dto.officeId,
            metricType = metricType,
            ranges = dto.ranges.orEmpty().mapNotNull { range ->
                WellbeingEnumMapper.indicator(range.indicator)?.let { ThresholdRange(it, range.minValue, range.maxValue) }
            }
        )
    }

    fun toDefineRequest(ranges: List<ThresholdRange>): DefineMetricThresholdRequestDto =
        DefineMetricThresholdRequestDto(
            ranges = ranges.map { ThresholdRangeDto(indicator = it.indicator.name, minValue = it.minValue, maxValue = it.maxValue) }
        )
}
