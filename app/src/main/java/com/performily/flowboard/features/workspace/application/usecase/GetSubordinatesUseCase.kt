package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import javax.inject.Inject

class GetSubordinatesUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(id: EmployeeId, onlyActive: Boolean = true): Result<List<Employee>> {
        return repository.getSubordinates(id)
            .map { employees ->
                employees.filter { !onlyActive || it.isActive }.sortedBy { it.name.sortableName }
            }
    }
}
