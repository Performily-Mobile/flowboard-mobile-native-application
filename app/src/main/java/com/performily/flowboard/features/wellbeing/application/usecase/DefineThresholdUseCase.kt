package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.repository.ThresholdRepository
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import javax.inject.Inject

/**
 * MA-74 - Defines or redefines the ranges of a metric (US49).
 *
 * Validates before sending with the same rules as the backend, which validates again.
 */
class DefineThresholdUseCase @Inject constructor(
    private val repository: ThresholdRepository,
    private val validateRanges: ValidateThresholdRangesUseCase
) {
    /**
     * @param officeId office that owns the threshold
     * @param metricType metric to configure
     * @param ranges new ranges of the metric
     * @return the saved threshold, or a failure with the first validation message
     */
    suspend operator fun invoke(officeId: Long, metricType: MetricType, ranges: List<ThresholdRange>): Result<MetricThreshold> {
        val validation = validateRanges(metricType, ranges)
        if (!validation.isValid) {
            val message = validation.generalError ?: validation.rowErrors.values.first()
            return Result.failure(IllegalArgumentException(message))
        }
        return repository.defineThreshold(officeId, metricType, ranges)
    }
}
