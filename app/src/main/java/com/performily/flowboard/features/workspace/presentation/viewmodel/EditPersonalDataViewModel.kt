package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.EmailAddress
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeeByIdUseCase
import com.performily.flowboard.features.workspace.application.usecase.UpdateEmployeePersonalDataUseCase
import com.performily.flowboard.features.workspace.domain.valueobject.Address
import com.performily.flowboard.features.workspace.domain.valueobject.BirthDate
import com.performily.flowboard.features.workspace.domain.valueobject.PersonName
import com.performily.flowboard.features.workspace.domain.valueobject.PhoneNumber
import com.performily.flowboard.features.workspace.presentation.state.EditPersonalDataUiState
import com.performily.flowboard.features.workspace.presentation.state.PersonalDataField
import com.performily.flowboard.features.workspace.presentation.ui.components.label
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditPersonalDataViewModel @Inject constructor(
    private val getEmployeeById: GetEmployeeByIdUseCase,
    private val updatePersonalData: UpdateEmployeePersonalDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditPersonalDataUiState())
    val state: StateFlow<EditPersonalDataUiState> = _state.asStateFlow()

    private var employeeId: EmployeeId? = null

    fun load(id: Long) {
        if (employeeId?.value == id) return
        employeeId = EmployeeId(id)
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getEmployeeById(EmployeeId(id))
                .onSuccess { employee ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            identityDocumentLabel = "${employee.identityDocument.type.label()} ${employee.identityDocument.number}",
                            firstName = employee.name.firstName,
                            lastName = employee.name.lastName,
                            birthDate = employee.birthDate.value,
                            email = employee.email.value,
                            phoneNumber = employee.phoneNumber.value,
                            street = employee.address.street.orEmpty(),
                            district = employee.address.district.orEmpty(),
                            province = employee.address.province.orEmpty(),
                            department = employee.address.department.orEmpty()
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onFirstNameChange(value: String) = updateField(PersonalDataField.FIRST_NAME) { it.copy(firstName = value) }

    fun onLastNameChange(value: String) = updateField(PersonalDataField.LAST_NAME) { it.copy(lastName = value) }

    fun onBirthDateChange(value: LocalDate) = updateField(PersonalDataField.BIRTH_DATE) { it.copy(birthDate = value) }

    fun onEmailChange(value: String) = updateField(PersonalDataField.EMAIL) { it.copy(email = value.trim()) }

    fun onPhoneChange(value: String) = updateField(PersonalDataField.PHONE) { it.copy(phoneNumber = value) }

    fun onStreetChange(value: String) = _state.update { it.copy(street = value) }

    fun onDistrictChange(value: String) = _state.update { it.copy(district = value) }

    fun onProvinceChange(value: String) = _state.update { it.copy(province = value) }

    fun onDepartmentChange(value: String) = _state.update { it.copy(department = value) }

    fun onSave() {
        val id = employeeId ?: return
        val current = _state.value
        val errors = mutableMapOf<PersonalDataField, String>()

        val name = validate(errors, PersonalDataField.FIRST_NAME) { PersonName(current.firstName.trim(), current.lastName.trim()) }
        val birthDate = validate(errors, PersonalDataField.BIRTH_DATE) {
            BirthDate(requireNotNull(current.birthDate) { "Selecciona la fecha de nacimiento." })
        }
        val email = validate(errors, PersonalDataField.EMAIL) { EmailAddress(current.email) }
        val phone = validate(errors, PersonalDataField.PHONE) { PhoneNumber.of(current.phoneNumber) }
        // PersonName valida nombres y apellidos juntos: ubicamos el error en el campo correcto.
        errors[PersonalDataField.FIRST_NAME]?.let { message ->
            if (message.contains("apellido", ignoreCase = true)) {
                errors.remove(PersonalDataField.FIRST_NAME)
                errors[PersonalDataField.LAST_NAME] = message
            }
        }

        if (errors.isNotEmpty() || name == null || birthDate == null || email == null || phone == null) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }

        val address = Address(
            street = current.street.trim(),
            district = current.district.trim(),
            province = current.province.trim(),
            department = current.department.trim()
        )
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            updatePersonalData(id, name, birthDate, email, phone, address)
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSaving = false, errorMessage = exception.message ?: "No se pudo guardar.")
                    }
                }
        }
    }

    private fun <T> validate(
        errors: MutableMap<PersonalDataField, String>,
        field: PersonalDataField,
        block: () -> T
    ): T? = try {
        block()
    } catch (exception: IllegalArgumentException) {
        errors[field] = exception.message ?: "Dato inválido."
        null
    }

    private fun updateField(
        field: PersonalDataField,
        transform: (EditPersonalDataUiState) -> EditPersonalDataUiState
    ) = _state.update { transform(it).copy(fieldErrors = it.fieldErrors - field) }
}
