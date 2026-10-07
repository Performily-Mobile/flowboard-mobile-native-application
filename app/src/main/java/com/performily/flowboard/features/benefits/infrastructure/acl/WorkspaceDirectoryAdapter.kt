package com.performily.flowboard.features.benefits.infrastructure.acl

import com.performily.flowboard.features.benefits.domain.entity.AreaOption
import com.performily.flowboard.features.benefits.domain.entity.EmployeeOption
import com.performily.flowboard.features.benefits.domain.repository.WorkspaceDirectory
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeesUseCase
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import javax.inject.Inject

/**
 * Capa anticorrupción hacia Workspace: usa sus casos de uso y traduce sus
 * entidades a los datos mínimos que Benefits necesita para asignar.
 */
class WorkspaceDirectoryAdapter @Inject constructor(
    private val getAreas: GetAreasUseCase,
    private val getEmployees: GetEmployeesUseCase
) : WorkspaceDirectory {

    override suspend fun getActiveAreas(): Result<List<AreaOption>> =
        getAreas(onlyActive = true).map { areas ->
            areas.map { AreaOption(id = it.id, name = it.name, activeEmployees = it.activeEmployees) }
        }

    override suspend fun getActiveEmployees(): Result<List<EmployeeOption>> =
        getEmployees(status = EmploymentStatus.ACTIVE).map { employees ->
            employees.map { EmployeeOption(id = it.id.value, name = it.name.fullName, areaName = it.areaName) }
        }
}
