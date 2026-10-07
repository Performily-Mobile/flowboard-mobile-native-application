package com.performily.flowboard.features.wellbeing.domain.entity

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange

/** Rangos configurados para una métrica de un espacio. */
data class MetricThreshold(
    val id: Long,
    val officeId: Long,
    val metricType: MetricType,
    val ranges: List<ThresholdRange>
) {
    fun rangeOf(indicator: HealthIndicator): ThresholdRange? = ranges.firstOrNull { it.indicator == indicator }
}
