package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import javax.inject.Inject

class GetJobAssignmentsUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(employeeId: EmployeeId): Result<List<JobAssignment>> {
        return repository.getJobAssignments(employeeId)
            .map { assignments -> assignments.sortedByDescending { it.startDate } }
    }
}
