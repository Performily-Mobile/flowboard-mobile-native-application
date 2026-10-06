package com.performily.flowboard.features.workspace.domain.repository

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.EmployeeDocument
import com.performily.flowboard.features.workspace.domain.entity.JobAssignment
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
import java.time.LocalDate

interface EmployeeRepository {

    suspend fun getEmployees(
        search: String?,
        areaId: Long?,
        status: EmploymentStatus?,
        positionId: Long?
    ): Result<List<Employee>>

    suspend fun getEmployeeById(id: EmployeeId): Result<Employee>

    suspend fun registerEmployee(
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
    ): Result<Employee>

    suspend fun updatePersonalData(
        id: EmployeeId,
        name: PersonName,
        birthDate: BirthDate,
        email: EmailAddress,
        phoneNumber: PhoneNumber,
        address: Address
    ): Result<Employee>

    suspend fun assignJob(
        id: EmployeeId,
        areaId: Long,
        positionId: Long,
        effectiveDate: LocalDate
    ): Result<JobAssignment>

    suspend fun assignDirectManager(id: EmployeeId, managerId: EmployeeId): Result<Employee>

    suspend fun removeDirectManager(id: EmployeeId): Result<Employee>

    suspend fun getSubordinates(id: EmployeeId): Result<List<Employee>>

    suspend fun terminate(id: EmployeeId, termination: TerminationDetails): Result<Employee>

    suspend fun reinstate(
        id: EmployeeId,
        areaId: Long,
        positionId: Long,
        reinstatementDate: LocalDate
    ): Result<Employee>

    suspend fun getJobAssignments(employeeId: EmployeeId): Result<List<JobAssignment>>

    suspend fun getDocuments(employeeId: EmployeeId): Result<List<EmployeeDocument>>

    suspend fun attachDocument(
        employeeId: EmployeeId,
        documentType: DocumentType,
        file: FileReference
    ): Result<EmployeeDocument>

    suspend fun getOrganizationChart(areaId: Long?): Result<OrganizationChart>
}
