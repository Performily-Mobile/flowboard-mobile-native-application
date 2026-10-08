package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.repository.ThresholdRepository
import javax.inject.Inject

/** MA-74 · Umbrales configurados en un espacio (US49). */
class GetThresholdsUseCase @Inject constructor(private val repository: ThresholdRepository) {
    suspend operator fun invoke(officeId: Long): Result<List<MetricThreshold>> = repository.getThresholds(officeId)
}
