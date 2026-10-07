package com.performily.flowboard.features.wellbeing.application.usecase

import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.repository.ThresholdRepository
import com.performily.flowboard.features.wellbeing.domain.service.ThresholdRangesValidator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import javax.inject.Inject

/**
 * MA-74 · Define o redefine los rangos de una métrica (US49). Valida antes de enviar
 * con las mismas reglas del backend, que igual vuelve a validar.
 */
class DefineThresholdUseCase @Inject constructor(private val repository: ThresholdRepository) {
    private val validator = ThresholdRangesValidator()

    suspend operator fun invoke(officeId: Long, metricType: MetricType, ranges: List<ThresholdRange>): Result<MetricThreshold> {
        val validation = validator.validate(metricType, ranges)
        if (!validation.isValid) {
            val message = validation.generalError ?: validation.rowErrors.values.first()
            return Result.failure(IllegalArgumentException(message))
        }
        return repository.defineThreshold(officeId, metricType, ranges)
    }
}
