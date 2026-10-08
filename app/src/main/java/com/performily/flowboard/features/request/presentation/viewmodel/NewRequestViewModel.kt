package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.application.usecase.GetApproverPreviewUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestTypesUseCase
import com.performily.flowboard.features.request.application.usecase.GetVacationAvailabilityUseCase
import com.performily.flowboard.features.request.application.usecase.StoreAttachmentUseCase
import com.performily.flowboard.features.request.application.usecase.SubmitRequestUseCase
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.presentation.state.NewRequestStep
import com.performily.flowboard.features.request.presentation.state.NewRequestUiState
import com.performily.flowboard.features.request.presentation.ui.components.PickedFile
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class NewRequestViewModel @Inject constructor(
    private val getRequestTypes: GetRequestTypesUseCase,
    private val getVacationAvailability: GetVacationAvailabilityUseCase,
    private val getApproverPreview: GetApproverPreviewUseCase,
    private val storeAttachment: StoreAttachmentUseCase,
    private val submitRequest: SubmitRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewRequestUiState())
    val state: StateFlow<NewRequestUiState> = _state.asStateFlow()

    /** Carga los tipos activos, el saldo de vacaciones y quién recibirá la solicitud. */
    fun load() {
        if (_state.value.types.isNotEmpty()) return
        _state.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            val availability = async { getVacationAvailability().getOrNull() }
            val approver = async { getApproverPreview().getOrNull() }
            getRequestTypes(activeOnly = true)
                .onSuccess { types ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            types = types,
                            selectedType = it.selectedType ?: types.firstOrNull(),
                            availability = availability.await(),
                            approver = approver.await()
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            loadError = exception.requestMessage(RequestAction.LOAD, "No se pudieron cargar los tipos de solicitud.")
                        )
                    }
                }
        }
    }


    fun onTypeSelected(type: RequestType) = _state.update { it.copy(selectedType = type) }

    fun continueToDetail() {
        val type = _state.value.selectedType ?: return
        _state.update { state ->
            // Si cambió de tipo, se limpian los valores del formulario anterior.
            val sameFields = state.values.keys.all { key -> type.fields.any { it.key == key } }
            state.copy(
                step = NewRequestStep.DETAIL,
                values = if (sameFields) state.values else emptyMap(),
                fieldErrors = emptyMap(),
                byHours = state.byHours && !type.deductsBalance,
                errorMessage = null
            )
        }
    }


    fun onStartDateChange(date: LocalDate) = _state.update { state ->
        val end = state.endDate?.takeIf { !it.isBefore(date) } ?: date
        state.copy(startDate = date, endDate = end, periodError = null)
    }

    fun onEndDateChange(date: LocalDate) = _state.update { it.copy(endDate = date, periodError = null) }

    fun onByHoursChange(value: Boolean) = _state.update { it.copy(byHours = value, periodError = null) }

    fun onStartTimeChange(time: LocalTime) = _state.update { it.copy(startTime = time, periodError = null) }

    fun onEndTimeChange(time: LocalTime) = _state.update { it.copy(endTime = time, periodError = null) }

    fun onFieldChange(key: String, value: String) = _state.update {
        it.copy(values = it.values + (key to value), fieldErrors = it.fieldErrors - key)
    }

    fun onFilePicked(file: PickedFile) {
        _state.update { it.copy(isUploading = true, attachmentError = null) }
        viewModelScope.launch {
            storeAttachment(file.uri, file.name, file.contentType, file.sizeInBytes)
                .onSuccess { stored -> _state.update { it.copy(isUploading = false, attachments = it.attachments + stored) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isUploading = false, attachmentError = exception.message ?: "No se pudo adjuntar el archivo.")
                    }
                }
        }
    }

    fun removeAttachment(file: FileReference) = _state.update { it.copy(attachments = it.attachments - file) }

    fun continueToConfirmation() {
        val state = _state.value
        val type = state.selectedType ?: return
        val period = state.period
        if (period == null) {
            _state.update { it.copy(periodError = periodProblem(it)) }
            return
        }
        val fieldErrors = type.fields.mapNotNull { field ->
            val value = state.values[field.key]?.trim().orEmpty()
            when {
                field.required && value.isEmpty() -> field.key to "Este campo es obligatorio."
                value.isNotEmpty() && !field.dataType.isValid(value) -> field.key to "Revisa el formato."
                else -> null
            }
        }.toMap()
        val attachmentError = if (type.requiresAttachment && state.attachments.isEmpty()) {
            "Este tipo de solicitud requiere un documento adjunto."
        } else {
            null
        }
        if (fieldErrors.isNotEmpty() || attachmentError != null) {
            _state.update { it.copy(fieldErrors = fieldErrors, attachmentError = attachmentError) }
            return
        }
        _state.update { it.copy(step = NewRequestStep.CONFIRMATION, errorMessage = null) }
    }

    private fun periodProblem(state: NewRequestUiState): String = when {
        state.startDate == null -> "Elige la fecha de inicio."
        state.byHours && state.canUseHours && (state.startTime == null || state.endTime == null) -> "Indica la hora de inicio y la de fin."
        state.byHours && state.canUseHours -> "La hora de fin debe ser posterior a la de inicio."
        state.endDate == null -> "Elige la fecha de fin."
        else -> "La fecha de fin no puede ser anterior a la de inicio."
    }

    fun submit() {
        val state = _state.value
        val type = state.selectedType ?: return
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            submitRequest(type, state.period, state.fieldValues, state.attachments)
                .onSuccess { request ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            resultMessage = "Solicitud ${RequestFormatters.code(request.id)} enviada a ${RequestFormatters.shortName(state.approverName)}."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = exception.requestMessage(RequestAction.SUBMIT, "No se pudo enviar la solicitud.")
                        )
                    }
                }
        }
    }

    fun goBack(): Boolean {
        val previous = when (_state.value.step) {
            NewRequestStep.TYPE -> return false
            NewRequestStep.DETAIL -> NewRequestStep.TYPE
            NewRequestStep.CONFIRMATION -> NewRequestStep.DETAIL
        }
        _state.update { it.copy(step = previous, errorMessage = null) }
        return true
    }
}
