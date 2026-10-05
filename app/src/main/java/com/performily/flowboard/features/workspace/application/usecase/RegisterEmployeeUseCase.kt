package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentPeriod
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import javax.inject.Inject

class RegisterEmployeeUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(
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
        return repository.registerEmployee(
            name = name,
            identityDocument = identityDocument,
            birthDate = birthDate,
            email = email,
            phoneNumber = phoneNumber,
            address = address,
            contractType = contractType,
            employmentPeriod = employmentPeriod,
            areaId = areaId,
            positionId = positionId,
            directManagerId = directManagerId
        )
    }
}
