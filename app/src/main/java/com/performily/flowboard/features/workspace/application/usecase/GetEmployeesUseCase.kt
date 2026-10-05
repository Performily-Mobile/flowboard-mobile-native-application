package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import javax.inject.Inject

class GetEmployeesUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(
        search: String? = null,
        areaId: Long? = null,
        status: EmploymentStatus? = null,
        positionId: Long? = null
    ): Result<List<Employee>> {
        return repository.getEmployees(search?.trim()?.takeIf { it.isNotEmpty() }, areaId, status, positionId)
            .map { employees -> employees.sortedBy { it.name.sortableName.lowercase() } }
    }
}
