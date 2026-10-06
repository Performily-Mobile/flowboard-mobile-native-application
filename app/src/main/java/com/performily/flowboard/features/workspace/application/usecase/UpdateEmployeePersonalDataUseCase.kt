package com.performily.flowboard.features.workspace.application.usecase

import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.repository.EmployeeRepository
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import javax.inject.Inject

class UpdateEmployeePersonalDataUseCase @Inject constructor(private val repository: EmployeeRepository) {

    suspend operator fun invoke(
        id: EmployeeId,
        name: PersonName,
        birthDate: BirthDate,
        email: EmailAddress,
        phoneNumber: PhoneNumber,
        address: Address
    ): Result<Employee> {
        return repository.updatePersonalData(id, name, birthDate, email, phoneNumber, address)
    }
}
