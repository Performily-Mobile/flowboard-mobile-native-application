package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import javax.inject.Inject

class GetCurrentEmployeeUseCase @Inject constructor(
    private val currentEmployeeProvider: CurrentEmployeeProvider,
    private val repository: EmployeeRepository
) {

    suspend operator fun invoke(): Result<Employee> =
        repository.getEmployeeById(currentEmployeeProvider.currentEmployeeId())
}
