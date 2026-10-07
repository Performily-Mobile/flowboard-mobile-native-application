package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.request.application.usecase.CreateRequestTypeUseCase
import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType
import com.performily.flowboard.features.request.presentation.state.FieldForm
import com.performily.flowboard.features.request.presentation.state.NewRequestTypeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class NewRequestTypeViewModel @Inject constructor(
    private val createRequestType: CreateRequestTypeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewRequestTypeUiState())
    val state: StateFlow<NewRequestTypeUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value.take(MAX_NAME), nameError = null) }

    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value.take(MAX_DESCRIPTION)) }

    fun onRequiresAttachmentChange(value: Boolean) = _state.update { it.copy(requiresAttachment = value) }

    fun onBalanceDeductionChange(value: BalanceDeduction) = _state.update { it.copy(balanceDeduction = value) }


    fun showFieldSheet() = _state.update { it.copy(fieldForm = FieldForm()) }

    fun dismissFieldSheet() = _state.update { it.copy(fieldForm = null) }

    fun onFieldLabelChange(value: String) = _state.update { it.copy(fieldForm = it.fieldForm?.copy(label = value, error = null)) }

    fun onFieldDataTypeChange(value: FieldDataType) = _state.update { it.copy(fieldForm = it.fieldForm?.copy(dataType = value)) }

    fun onFieldRequiredChange(value: Boolean) = _state.update { it.copy(fieldForm = it.fieldForm?.copy(required = value)) }

    fun addField() {
        val state = _state.value
        val form = state.fieldForm ?: return
        val label = form.label.trim()
        val error = when {
            label.isEmpty() -> "Ingresa el nombre del campo."
            state.fields.any { it.label.equals(label, ignoreCase = true) } -> "Ya hay un campo con este nombre."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(fieldForm = form.copy(error = error)) }
            return
        }
        // La clave en camelCase la arma la app a partir del nombre: "Motivo del viaje" -> "motivoDelViaje".
        val key = CreateRequestTypeUseCase.keyFor(label, state.fields.map { it.key })
        val field = NewRequestField(key = key, label = label, dataType = form.dataType, required = form.required)
        _state.update { it.copy(fields = it.fields + field, fieldForm = null) }
    }

    fun removeField(field: NewRequestField) = _state.update { it.copy(fields = it.fields - field) }

    fun save() {
        val state = _state.value
        if (state.name.isBlank()) {
            _state.update { it.copy(nameError = "Ingresa el nombre del tipo de solicitud.") }
            return
        }
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            createRequestType(
                name = state.name,
                description = state.description,
                requiresAttachment = state.requiresAttachment,
                balanceDeduction = state.balanceDeduction,
                fields = state.fields
            )
                .onSuccess { type ->
                    _state.update { it.copy(isSaving = false, resultMessage = "Tipo \"${type.name}\" creado.") }
                }
                .onFailure { exception ->
                    val message = exception.requestMessage(RequestAction.CREATE_TYPE, "No se pudo crear el tipo de solicitud.")
                    _state.update {
                        if (message.contains("nombre")) it.copy(isSaving = false, nameError = message)
                        else it.copy(isSaving = false, errorMessage = message)
                    }
                }
        }
    }

    private companion object {
        const val MAX_NAME = 80
        const val MAX_DESCRIPTION = 255
    }
}
