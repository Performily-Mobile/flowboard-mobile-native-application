package com.performily.flowboard.features.request.infrastructure.acl

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.request.domain.entity.RequestPerson
import com.performily.flowboard.features.request.domain.repository.RequestEmployeeDirectory
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeByIdUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeesUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetSubordinatesUseCase
import com.performily.flowboard.features.workspace.domain.entity.Employee
import javax.inject.Inject


class WorkspaceEmployeeDirectoryAdapter @Inject constructor(
    private val getEmployeeById: GetEmployeeByIdUseCase,
    private val getEmployees: GetEmployeesUseCase,
    private val getSubordinates: GetSubordinatesUseCase
) : RequestEmployeeDirectory {

    override suspend fun getPerson(employeeId: Long): Result<RequestPerson> =
        getEmployeeById(EmployeeId(employeeId)).map(::toPerson)

    override suspend fun getPeople(): Result<List<RequestPerson>> =
        getEmployees().map { employees -> employees.map(::toPerson) }

    override suspend fun getTeam(managerId: Long): Result<List<RequestPerson>> =
        getSubordinates(EmployeeId(managerId), onlyActive = false).map { employees -> employees.map(::toPerson) }

    private fun toPerson(employee: Employee) = RequestPerson(
        id = employee.id.value,
        name = employee.name.fullName,
        jobDescription = employee.jobDescription,
        directManagerId = employee.directManagerId?.value
    )
}
