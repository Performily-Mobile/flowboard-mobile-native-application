package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.TerminationDetails
import javax.inject.Inject

class TerminateEmployeeUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(id: EmployeeId, termination: TerminationDetails): Result<Employee> {
        return repository.terminate(id, termination)
    }
}
