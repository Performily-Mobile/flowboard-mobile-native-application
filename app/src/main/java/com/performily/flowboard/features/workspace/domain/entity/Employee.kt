package com.performily.flowboard.features.workspace.domain.entity

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentPeriod
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import com.performily.flowboard.features.workspace.domain.valueobject.TerminationDetails
import java.time.LocalDateTime

data class Employee(
    val id: EmployeeId,
    val name: PersonName,
    val identityDocument: IdentityDocument,
    val birthDate: BirthDate,
    val email: EmailAddress,
    val phoneNumber: PhoneNumber,
    val address: Address,
    val contractType: ContractType,
    val employmentPeriod: EmploymentPeriod,
    val status: EmploymentStatus,
    val termination: TerminationDetails?,
    val areaId: Long,
    val areaName: String,
    val positionId: Long,
    val positionTitle: String,
    val directManagerId: EmployeeId?,
    val updatedAt: LocalDateTime?
) {
    val isActive: Boolean get() = status == EmploymentStatus.ACTIVE

    val isTerminated: Boolean get() = status == EmploymentStatus.TERMINATED

    val hasDirectManager: Boolean get() = directManagerId != null

    fun isManagedBy(managerId: EmployeeId): Boolean = directManagerId == managerId

    val jobDescription: String get() = "$positionTitle · $areaName"
}
