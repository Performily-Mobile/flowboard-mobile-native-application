package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import javax.inject.Inject

class RemoveDirectManagerUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(id: EmployeeId): Result<Employee> = repository.removeDirectManager(id)
}
