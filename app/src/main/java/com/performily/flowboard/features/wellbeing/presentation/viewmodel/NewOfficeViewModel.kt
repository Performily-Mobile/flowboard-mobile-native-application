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

/** MA-71 · Registro de un espacio (US47). */
@HiltViewModel
class NewOfficeViewModel @Inject constructor(
    private val createOffice: CreateOfficeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewOfficeUiState())
    val state: StateFlow<NewOfficeUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null, errorMessage = null) }

    fun onAreaChange(value: String) = _state.update { it.copy(area = value) }

    fun onAddressChange(value: String) = _state.update { it.copy(address = value, addressError = null) }

    fun onFloorChange(value: String) = _state.update { it.copy(floor = value, floorError = null) }

    fun onReferenceChange(value: String) = _state.update { it.copy(reference = value) }

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
                        // Un nombre repetido se marca en el propio campo, como en el prototipo.
                        if ((exception as? ApiException)?.code == "OFFICE_CONFLICT") {
                            it.copy(isSubmitting = false, nameError = message)
                        } else {
                            it.copy(isSubmitting = false, errorMessage = message)
                        }
                    }
                }
        }
    }
}
