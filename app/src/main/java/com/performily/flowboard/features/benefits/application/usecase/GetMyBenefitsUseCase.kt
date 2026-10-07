package com.performily.flowboard.features.benefits.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import javax.inject.Inject

/** MA-58 / MA-59 · Beneficios vigentes y entregados del colaborador que usa la app (US40). */
class GetMyBenefitsUseCase @Inject constructor(
    private val repository: BenefitAssignmentRepository,
    private val currentEmployee: CurrentEmployeeProvider
) {
    suspend operator fun invoke(): Result<EmployeeBenefits> =
        repository.getEmployeeBenefits(currentEmployee.currentEmployeeId().value)
}
