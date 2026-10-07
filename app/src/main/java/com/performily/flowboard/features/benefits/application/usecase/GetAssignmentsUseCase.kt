package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import javax.inject.Inject

/** MA-62 · Asignaciones por entregar o entregadas, ordenadas por colaborador. */
class GetAssignmentsUseCase @Inject constructor(private val repository: BenefitAssignmentRepository) {
    suspend operator fun invoke(status: AssignmentStatus): Result<List<BenefitAssignment>> =
        repository.getAssignments(status).map { list ->
            if (status == AssignmentStatus.DELIVERED) {
                list.sortedByDescending { it.delivery?.deliveredOn }
            } else {
                list.sortedWith(compareBy({ it.employeeName.lowercase() }, { it.benefitTypeName.lowercase() }))
            }
        }
}
