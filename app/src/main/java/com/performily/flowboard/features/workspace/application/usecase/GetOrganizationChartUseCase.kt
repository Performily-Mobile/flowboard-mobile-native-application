package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChart
import javax.inject.Inject

class GetOrganizationChartUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(areaId: Long? = null): Result<OrganizationChart> =
        repository.getOrganizationChart(areaId)
}
