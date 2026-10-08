package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.AreaAssignmentPreview
import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import java.time.LocalDate
import javax.inject.Inject

/** MA-61 · Cuántos colaboradores del área recibirán el beneficio y cuántos se omitirán. */
class PreviewAreaAssignmentUseCase @Inject constructor(private val repository: BenefitAssignmentRepository) {
    suspend operator fun invoke(benefitTypeId: Long, areaId: Long, startDate: LocalDate, endDate: LocalDate): Result<AreaAssignmentPreview> {
        if (endDate.isBefore(startDate)) {
            return Result.failure(IllegalArgumentException("La fecha final no puede ser anterior a la inicial."))
        }
        return repository.previewAreaAssignment(benefitTypeId, areaId, startDate, endDate)
    }
}
