package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocumentType
import java.time.LocalDate

enum class RegisterField {
    FIRST_NAME,
    LAST_NAME,
    DOCUMENT_NUMBER,
    BIRTH_DATE,
    EMAIL,
    PHONE,
    HIRE_DATE,
    CONTRACT_END_DATE,
    AREA,
    POSITION
}

data class RegisterEmployeeUiState(
    val step: Int = 1,
    val firstName: String = "",
    val lastName: String = "",
    val documentType: IdentityDocumentType = IdentityDocumentType.DNI,
    val documentNumber: String = "",
    val birthDate: LocalDate? = null,
    val email: String = "",
    val phoneNumber: String = "",
    val contractType: ContractType = ContractType.INDEFINITE,
    val hireDate: LocalDate? = LocalDate.now(),
    val contractEndDate: LocalDate? = null,
    val areaId: Long? = null,
    val positionId: Long? = null,
    val directManagerId: EmployeeId? = null,
    val areas: List<Area> = emptyList(),
    val positions: List<Position> = emptyList(),
    val managers: List<Employee> = emptyList(),
    val fieldErrors: Map<RegisterField, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val registeredEmployeeId: Long? = null
) {
    val selectedPosition: Position? get() = positions.firstOrNull { it.id == positionId }

    fun errorOf(field: RegisterField): String? = fieldErrors[field]
}
