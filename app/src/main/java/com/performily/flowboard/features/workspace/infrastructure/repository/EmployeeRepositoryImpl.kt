package com.performily.flowboard.features.workspace.infrastructure.repository

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentPeriod
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChart
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import com.performily.flowboard.features.workspace.infrastructure.mapper.EmployeeDocumentMapper
import com.performily.flowboard.features.workspace.infrastructure.mapper.EmployeeMapper
import com.performily.flowboard.features.workspace.infrastructure.mapper.JobAssignmentMapper
import com.performily.flowboard.features.workspace.infrastructure.mapper.OrganizationChartMapper
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeDocumentService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeJobAssignmentService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeService
import javax.inject.Inject

class EmployeeRepositoryImpl @Inject constructor(
    private val employeeService: EmployeeService,
    private val jobAssignmentService: EmployeeJobAssignmentService,
    private val documentService: EmployeeDocumentService
) : EmployeeRepository {

    override suspend fun getEmployees(
        search: String?,
        areaId: Long?,
        status: EmploymentStatus?,
        positionId: Long?
    ): Result<List<Employee>> {
        return apiCall { employeeService.getEmployees(search, areaId, status?.name, positionId) }
            .mapCatching { dtos -> dtos.map { EmployeeMapper.toDomain(it) } }
    }

    override suspend fun getEmployeeById(id: EmployeeId): Result<Employee> {
        return apiCall { employeeService.getEmployeeById(id.value) }
            .mapCatching { dto -> EmployeeMapper.toDomain(dto) }
    }

    override suspend fun registerEmployee(
        name: PersonName,
        identityDocument: IdentityDocument,
        birthDate: BirthDate,
        email: EmailAddress,
        phoneNumber: PhoneNumber,
        address: Address,
        contractType: ContractType,
        employmentPeriod: EmploymentPeriod,
        areaId: Long,
        positionId: Long,
        directManagerId: EmployeeId?
    ): Result<Employee> {
        val request = EmployeeMapper.toRegisterRequest(
            name, identityDocument, birthDate, email, phoneNumber, address,
            contractType, employmentPeriod, areaId, positionId, directManagerId
        )
        return apiCall { employeeService.registerEmployee(request) }
            .mapCatching { dto -> EmployeeMapper.toDomain(dto) }
    }

    override suspend fun getJobAssignments(employeeId: EmployeeId): Result<List<JobAssignment>> {
        return apiCall { jobAssignmentService.getJobAssignments(employeeId.value) }
            .mapCatching { dtos -> dtos.map { JobAssignmentMapper.toDomain(it) } }
    }

    override suspend fun getDocuments(employeeId: EmployeeId): Result<List<EmployeeDocument>> {
        return apiCall { documentService.getDocuments(employeeId.value) }
            .mapCatching { dtos -> dtos.map { EmployeeDocumentMapper.toDomain(it) } }
    }

    override suspend fun getOrganizationChart(areaId: Long?): Result<OrganizationChart> {
        return apiCall { employeeService.getOrganizationChart(areaId) }
            .mapCatching { dto -> OrganizationChartMapper.toDomain(dto) }
    }
}
