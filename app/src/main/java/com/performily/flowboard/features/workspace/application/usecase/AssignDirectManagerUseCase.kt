package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import javax.inject.Inject

class AssignDirectManagerUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(id: EmployeeId, managerId: EmployeeId): Result<Employee> {
        if (id == managerId) {
            return Result.failure(IllegalArgumentException("Un colaborador no puede ser su propio jefe directo."))
        }
        return repository.assignDirectManager(id, managerId)
    }
}
