package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.service.ThresholdRangesValidator
import com.performily.flowboard.features.wellbeing.domain.service.ThresholdValidation
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import javax.inject.Inject

/**
 * MA-74 - Validates the ranges of a metric while they are being edited (US49).
 *
 * Applies the same rules as the backend so the problem can be shown on the exact row.
 */
class ValidateThresholdRangesUseCase @Inject constructor() {
    private val validator = ThresholdRangesValidator()

    /**
     * @param metricType metric whose physical range limits the values
     * @param ranges ranges to validate
     * @return the per-row errors and the general error, if any
     */
    operator fun invoke(metricType: MetricType, ranges: List<ThresholdRange>): ThresholdValidation =
        validator.validate(metricType, ranges)
}
