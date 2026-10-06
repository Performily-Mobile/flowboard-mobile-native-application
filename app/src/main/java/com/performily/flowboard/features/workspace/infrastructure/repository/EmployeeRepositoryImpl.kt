package com.performily.flowboard.features.workspace.infrastructure.repository

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.DocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentPeriod
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChart
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import com.performily.flowboard.features.workspace.domain.valueobject.TerminationDetails
import com.performily.flowboard.features.workspace.infrastructure.mapper.EmployeeDocumentMapper
import com.performily.flowboard.features.workspace.infrastructure.mapper.EmployeeMapper
import com.performily.flowboard.features.workspace.infrastructure.mapper.JobAssignmentMapper
import com.performily.flowboard.features.workspace.infrastructure.mapper.OrganizationChartMapper
import com.performily.flowboard.features.workspace.infrastructure.remote.AssignDirectManagerRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeDocumentService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeJobAssignmentService
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeService
import java.time.LocalDate
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

    override suspend fun updatePersonalData(
        id: EmployeeId,
        name: PersonName,
        birthDate: BirthDate,
        email: EmailAddress,
        phoneNumber: PhoneNumber,
        address: Address
    ): Result<Employee> {
        val request = EmployeeMapper.toUpdatePersonalDataRequest(name, birthDate, email, phoneNumber, address)
        return apiCall { employeeService.updatePersonalData(id.value, request) }
            .mapCatching { dto -> EmployeeMapper.toDomain(dto) }
    }

    override suspend fun assignJob(
        id: EmployeeId,
        areaId: Long,
        positionId: Long,
        effectiveDate: LocalDate
    ): Result<JobAssignment> {
        val request = JobAssignmentMapper.toAssignRequest(areaId, positionId, effectiveDate)
        return apiCall { jobAssignmentService.assignJob(id.value, request) }
            .mapCatching { dto -> JobAssignmentMapper.toDomain(dto) }
    }

    override suspend fun assignDirectManager(id: EmployeeId, managerId: EmployeeId): Result<Employee> {
        val request = AssignDirectManagerRequestDto(managerId = managerId.value)
        return apiCall { employeeService.assignDirectManager(id.value, request) }
            .mapCatching { dto -> EmployeeMapper.toDomain(dto) }
    }

    override suspend fun removeDirectManager(id: EmployeeId): Result<Employee> {
        return apiCall { employeeService.removeDirectManager(id.value) }
            .mapCatching { dto -> EmployeeMapper.toDomain(dto) }
    }

    override suspend fun getSubordinates(id: EmployeeId): Result<List<Employee>> {
        return apiCall { employeeService.getSubordinates(id.value) }
            .mapCatching { dtos -> dtos.map { EmployeeMapper.toDomain(it) } }
    }

    override suspend fun terminate(id: EmployeeId, termination: TerminationDetails): Result<Employee> {
        val request = EmployeeMapper.toTerminateRequest(termination)
        return apiCall { employeeService.terminate(id.value, request) }
            .mapCatching { dto -> EmployeeMapper.toDomain(dto) }
    }

    override suspend fun reinstate(
        id: EmployeeId,
        areaId: Long,
        positionId: Long,
        reinstatementDate: LocalDate
    ): Result<Employee> {
        val request = EmployeeMapper.toReinstateRequest(areaId, positionId, reinstatementDate)
        return apiCall { employeeService.reinstate(id.value, request) }
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

    override suspend fun attachDocument(
        employeeId: EmployeeId,
        documentType: DocumentType,
        file: FileReference
    ): Result<EmployeeDocument> {
        val request = EmployeeDocumentMapper.toAttachRequest(documentType, file)
        return apiCall { documentService.attachDocument(employeeId.value, request) }
            .mapCatching { dto -> EmployeeDocumentMapper.toDomain(dto) }
    }

    override suspend fun getOrganizationChart(areaId: Long?): Result<OrganizationChart> {
        return apiCall { employeeService.getOrganizationChart(areaId) }
            .mapCatching { dto -> OrganizationChartMapper.toDomain(dto) }
    }
}
