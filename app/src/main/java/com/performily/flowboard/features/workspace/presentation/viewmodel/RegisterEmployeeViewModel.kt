package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeesUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetPositionsUseCase
import com.performily.flowboard.features.workspace.application.usecase.RegisterEmployeeUseCase
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.ContractType
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentPeriod
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocumentType
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import com.performily.flowboard.features.workspace.presentation.state.RegisterEmployeeUiState
import com.performily.flowboard.features.workspace.presentation.state.RegisterField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RegisterEmployeeViewModel @Inject constructor(
    private val registerEmployee: RegisterEmployeeUseCase,
    private val getAreas: GetAreasUseCase,
    private val getPositions: GetPositionsUseCase,
    private val getEmployees: GetEmployeesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterEmployeeUiState())
    val state: StateFlow<RegisterEmployeeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAreas(onlyActive = true).onSuccess { areas -> _state.update { it.copy(areas = areas) } }
            getEmployees(status = EmploymentStatus.ACTIVE)
                .onSuccess { managers -> _state.update { it.copy(managers = managers) } }
        }
    }

    fun onFirstNameChange(value: String) = updateField(RegisterField.FIRST_NAME) { it.copy(firstName = value) }

    fun onLastNameChange(value: String) = updateField(RegisterField.LAST_NAME) { it.copy(lastName = value) }

    fun onDocumentTypeChange(value: IdentityDocumentType) =
        updateField(RegisterField.DOCUMENT_NUMBER) { it.copy(documentType = value) }

    fun onDocumentNumberChange(value: String) =
        updateField(RegisterField.DOCUMENT_NUMBER) { it.copy(documentNumber = value.trim()) }

    fun onBirthDateChange(value: LocalDate) = updateField(RegisterField.BIRTH_DATE) { it.copy(birthDate = value) }

    fun onEmailChange(value: String) = updateField(RegisterField.EMAIL) { it.copy(email = value.trim()) }

    fun onPhoneChange(value: String) = updateField(RegisterField.PHONE) { it.copy(phoneNumber = value) }

    fun onContractTypeChange(value: ContractType) = updateField(RegisterField.CONTRACT_END_DATE) {
        it.copy(contractType = value, contractEndDate = if (value == ContractType.FIXED_TERM) it.contractEndDate else null)
    }

    fun onHireDateChange(value: LocalDate) = updateField(RegisterField.HIRE_DATE) { it.copy(hireDate = value) }

    fun onContractEndDateChange(value: LocalDate) =
        updateField(RegisterField.CONTRACT_END_DATE) { it.copy(contractEndDate = value) }

    fun onAreaChange(areaId: Long) {
        updateField(RegisterField.AREA) { it.copy(areaId = areaId, positionId = null, positions = emptyList()) }
        viewModelScope.launch {
            getPositions(areaId = areaId, onlyActive = true)
                .onSuccess { positions -> _state.update { it.copy(positions = positions) } }
        }
    }

    fun onPositionChange(positionId: Long) = updateField(RegisterField.POSITION) { it.copy(positionId = positionId) }

    fun onDirectManagerChange(managerId: EmployeeId?) = _state.update { it.copy(directManagerId = managerId) }

    fun onContinue() {
        val errors = validatePersonalData()
        _state.update {
            it.copy(fieldErrors = errors, errorMessage = null, step = if (errors.isEmpty()) 2 else 1)
        }
    }

    fun onBack() = _state.update { it.copy(step = 1, errorMessage = null) }

    fun onSubmit() {
        val current = _state.value
        val errors = validatePersonalData() + validateContract()
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors, step = if (errors.keys.any { it in PERSONAL_FIELDS }) 1 else 2) }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            registerEmployee(
                name = PersonName(current.firstName.trim(), current.lastName.trim()),
                identityDocument = IdentityDocument(current.documentType, current.documentNumber),
                birthDate = BirthDate(requireNotNull(current.birthDate)),
                email = EmailAddress(current.email),
                phoneNumber = PhoneNumber.of(current.phoneNumber),
                address = Address(),
                contractType = current.contractType,
                employmentPeriod = EmploymentPeriod.of(
                    current.contractType,
                    requireNotNull(current.hireDate),
                    current.contractEndDate
                ),
                areaId = requireNotNull(current.areaId),
                positionId = requireNotNull(current.positionId),
                directManagerId = current.directManagerId
            )
                .onSuccess { employee ->
                    _state.update { it.copy(isSubmitting = false, registeredEmployeeId = employee.id.value) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isSubmitting = false, errorMessage = exception.message) }
                }
        }
    }

    private fun validatePersonalData(): Map<RegisterField, String> {
        val current = _state.value
        val errors = mutableMapOf<RegisterField, String>()
        validate(errors, RegisterField.FIRST_NAME) { PersonName(current.firstName.trim(), "Apellido") }
        validate(errors, RegisterField.LAST_NAME) { PersonName("Nombre", current.lastName.trim()) }
        validate(errors, RegisterField.DOCUMENT_NUMBER) { IdentityDocument(current.documentType, current.documentNumber) }
        validate(errors, RegisterField.BIRTH_DATE) {
            BirthDate(current.birthDate ?: throw IllegalArgumentException(REQUIRED))
        }
        validate(errors, RegisterField.EMAIL) { EmailAddress(current.email) }
        validate(errors, RegisterField.PHONE) { PhoneNumber.of(current.phoneNumber) }
        return errors
    }

    private fun validateContract(): Map<RegisterField, String> {
        val current = _state.value
        val errors = mutableMapOf<RegisterField, String>()
        if (current.hireDate == null) errors[RegisterField.HIRE_DATE] = REQUIRED
        validate(errors, RegisterField.CONTRACT_END_DATE) {
            current.hireDate?.let { EmploymentPeriod.of(current.contractType, it, current.contractEndDate) }
        }
        if (current.areaId == null) errors[RegisterField.AREA] = REQUIRED
        if (current.positionId == null) errors[RegisterField.POSITION] = REQUIRED
        return errors
    }

    private fun validate(errors: MutableMap<RegisterField, String>, field: RegisterField, block: () -> Unit) {
        try {
            block()
        } catch (exception: IllegalArgumentException) {
            errors[field] = exception.message ?: REQUIRED
        }
    }

    private fun updateField(field: RegisterField, transform: (RegisterEmployeeUiState) -> RegisterEmployeeUiState) {
        _state.update { transform(it).copy(fieldErrors = it.fieldErrors - field) }
    }

    private companion object {
        const val REQUIRED = "Este campo es obligatorio."
        val PERSONAL_FIELDS = setOf(
            RegisterField.FIRST_NAME,
            RegisterField.LAST_NAME,
            RegisterField.DOCUMENT_NUMBER,
            RegisterField.BIRTH_DATE,
            RegisterField.EMAIL,
            RegisterField.PHONE
        )
    }
}
