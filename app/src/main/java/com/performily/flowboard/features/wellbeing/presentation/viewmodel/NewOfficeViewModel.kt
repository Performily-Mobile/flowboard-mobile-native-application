package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.network.ApiException
import com.performily.flowboard.features.wellbeing.application.usecase.CreateOfficeUseCase
import com.performily.flowboard.features.wellbeing.presentation.state.NewOfficeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MA-71 - Registers a workspace (US47).
 *
 * Text inputs are truncated to the maximum lengths the backend validates.
 */
@HiltViewModel
class NewOfficeViewModel @Inject constructor(
    private val createOffice: CreateOfficeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewOfficeUiState())
    val state: StateFlow<NewOfficeUiState> = _state.asStateFlow()

    fun onNameChange(value: String) =
        _state.update { it.copy(name = value.take(MAX_NAME), nameError = null, errorMessage = null) }

    fun onAreaChange(value: String) = _state.update { it.copy(area = value) }

    fun onAddressChange(value: String) =
        _state.update { it.copy(address = value.take(MAX_ADDRESS), addressError = null) }

    fun onFloorChange(value: String) =
        _state.update { it.copy(floor = value.take(MAX_FLOOR), floorError = null) }

    fun onReferenceChange(value: String) = _state.update { it.copy(reference = value.take(MAX_REFERENCE)) }

    /**
     * Validates the form and creates the office.
     *
     * A repeated name is shown on the name field itself, as in the prototype.
     */
    fun onSubmit() {
        val form = _state.value
        val nameError = if (form.name.isBlank()) "Ingresa el nombre del espacio." else null
        val addressError = if (form.address.isBlank()) "Ingresa la dirección." else null
        val floorError = if (form.floor.isBlank()) "Ingresa el piso." else null
        if (nameError != null || addressError != null || floorError != null) {
            _state.update { it.copy(nameError = nameError, addressError = addressError, floorError = floorError) }
            return
        }

        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            createOffice(form.name, form.area, form.address, form.floor, form.reference)
                .onSuccess { office -> _state.update { it.copy(isSubmitting = false, createdOfficeId = office.id) } }
                .onFailure { exception ->
                    val message = exception.wellbeingMessage("No se pudo crear el espacio.")
                    _state.update {
                        if ((exception as? ApiException)?.code == "OFFICE_CONFLICT") {
                            it.copy(isSubmitting = false, nameError = message)
                        } else {
                            it.copy(isSubmitting = false, errorMessage = message)
                        }
                    }
                }
        }
    }

    private companion object {
        /** Maximum lengths validated by the backend (CreateOfficeResource). */
        const val MAX_NAME = 80
        const val MAX_ADDRESS = 150
        const val MAX_FLOOR = 30
        const val MAX_REFERENCE = 150
    }
}
