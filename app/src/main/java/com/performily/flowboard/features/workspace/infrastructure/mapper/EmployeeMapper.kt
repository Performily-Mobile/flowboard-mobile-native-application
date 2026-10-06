package com.performily.flowboard.features.workspace.infrastructure.mapper

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentPeriod
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import com.performily.flowboard.features.workspace.domain.valueobject.TerminationDetails
import com.performily.flowboard.features.workspace.infrastructure.remote.EmployeeDto
import com.performily.flowboard.features.workspace.infrastructure.remote.RegisterEmployeeRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.ReinstateEmployeeRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.TerminateEmployeeRequestDto
import com.performily.flowboard.features.workspace.infrastructure.remote.UpdateEmployeePersonalDataRequestDto
import java.time.LocalDate
import java.time.LocalDateTime

object EmployeeMapper {

    fun toDomain(dto: EmployeeDto): Employee {
        val terminationReason = dto.terminationReason
        val terminationDate = dto.terminationDate
        return Employee(
            id = EmployeeId(dto.id),
            name = PersonName(dto.firstName, dto.lastName),
            identityDocument = IdentityDocument(
                IdentityDocumentType.valueOf(dto.identityDocumentType),
                dto.identityDocumentNumber
            ),
            birthDate = BirthDate(LocalDate.parse(dto.birthDate)),
            email = EmailAddress(dto.email),
            phoneNumber = PhoneNumber.of(dto.phoneNumber),
            address = Address(dto.street, dto.district, dto.province, dto.department),
            contractType = ContractType.valueOf(dto.contractType),
            employmentPeriod = EmploymentPeriod(
                hireDate = LocalDate.parse(dto.hireDate),
                contractEndDate = dto.contractEndDate?.let(LocalDate::parse)
            ),
            status = EmploymentStatus.valueOf(dto.status),
            termination = if (terminationReason != null && terminationDate != null) {
                TerminationDetails(terminationReason, LocalDate.parse(terminationDate))
            } else {
                null
            },
            areaId = dto.areaId,
            areaName = dto.areaName.orEmpty(),
            positionId = dto.positionId,
            positionTitle = dto.positionTitle.orEmpty(),
            directManagerId = dto.directManagerId?.let(::EmployeeId),
            updatedAt = dto.updatedAt?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
        )
    }

    fun toRegisterRequest(
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
    ): RegisterEmployeeRequestDto {
        return RegisterEmployeeRequestDto(
            firstName = name.firstName.trim(),
            lastName = name.lastName.trim(),
            identityDocumentType = identityDocument.type.name,
            identityDocumentNumber = identityDocument.number,
            birthDate = birthDate.value.toString(),
            email = email.value,
            phoneNumber = phoneNumber.value,
            street = address.street?.takeIf { it.isNotBlank() },
            district = address.district?.takeIf { it.isNotBlank() },
            province = address.province?.takeIf { it.isNotBlank() },
            department = address.department?.takeIf { it.isNotBlank() },
            contractType = contractType.name,
            hireDate = employmentPeriod.hireDate.toString(),
            contractEndDate = employmentPeriod.contractEndDate?.toString(),
            areaId = areaId,
            positionId = positionId,
            directManagerId = directManagerId?.value
        )
    }

    fun toUpdatePersonalDataRequest(
        name: PersonName,
        birthDate: BirthDate,
        email: EmailAddress,
        phoneNumber: PhoneNumber,
        address: Address
    ): UpdateEmployeePersonalDataRequestDto {
        return UpdateEmployeePersonalDataRequestDto(
            firstName = name.firstName.trim(),
            lastName = name.lastName.trim(),
            birthDate = birthDate.value.toString(),
            email = email.value,
            phoneNumber = phoneNumber.value,
            street = address.street?.takeIf { it.isNotBlank() },
            district = address.district?.takeIf { it.isNotBlank() },
            province = address.province?.takeIf { it.isNotBlank() },
            department = address.department?.takeIf { it.isNotBlank() }
        )
    }

    fun toTerminateRequest(termination: TerminationDetails): TerminateEmployeeRequestDto =
        TerminateEmployeeRequestDto(
            reason = termination.reason.trim(),
            terminationDate = termination.terminationDate.toString()
        )

    fun toReinstateRequest(areaId: Long, positionId: Long, reinstatementDate: LocalDate): ReinstateEmployeeRequestDto =
        ReinstateEmployeeRequestDto(
            areaId = areaId,
            positionId = positionId,
            reinstatementDate = reinstatementDate.toString()
        )
}
