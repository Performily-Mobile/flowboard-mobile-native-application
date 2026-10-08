package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.application.usecase.CancelRequestUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestTypesUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestUseCase
import com.performily.flowboard.features.request.application.usecase.ResubmitRequestUseCase
import com.performily.flowboard.features.request.application.usecase.StoreAttachmentUseCase
import com.performily.flowboard.features.request.presentation.state.RequestDetailUiState
import com.performily.flowboard.features.request.presentation.ui.components.PickedFile
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val currentEmployee: CurrentEmployeeProvider,
    private val getRequest: GetRequestUseCase,
    private val getRequestTypes: GetRequestTypesUseCase,
    private val cancelRequest: CancelRequestUseCase,
    private val resubmitRequest: ResubmitRequestUseCase,
    private val storeAttachment: StoreAttachmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RequestDetailUiState())
    val state: StateFlow<RequestDetailUiState> = _state.asStateFlow()

    fun load(requestId: Long) {
        if (_state.value.requestId == requestId && _state.value.request != null) return
        _state.update {
            RequestDetailUiState(requestId = requestId, viewerId = currentEmployee.currentEmployeeId().value, isLoading = true)
        }
        viewModelScope.launch {
            // El tipo se pide para mostrar las etiquetas de los campos y saber si exige adjunto.
            val types = async { getRequestTypes().getOrNull().orEmpty() }
            getRequest(requestId)
                .onSuccess { request ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            request = request,
                            type = types.await().firstOrNull { type -> type.id == request.requestTypeId },
                            attachments = request.attachments
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.requestMessage(RequestAction.LOAD, "No se pudo cargar la solicitud.")
                        )
                    }
                }
        }
    }

    fun retry() {
        val requestId = _state.value.requestId ?: return
        _state.update { it.copy(requestId = null) }
        load(requestId)
    }


    fun showCancelDialog() = _state.update { it.copy(isCancelDialogVisible = true) }

    fun dismissCancelDialog() = _state.update { it.copy(isCancelDialogVisible = false) }

    fun confirmCancel() {
        val request = _state.value.request ?: return
        _state.update { it.copy(isCancelling = true) }
        viewModelScope.launch {
            cancelRequest(request)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            isCancelDialogVisible = false,
                            resultMessage = "Solicitud ${RequestFormatters.code(request.id)} cancelada."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            isCancelDialogVisible = false,
                            snackbarMessage = exception.requestMessage(RequestAction.CANCEL, "No se pudo cancelar la solicitud.")
                        )
                    }
                }
        }
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

    fun resubmit() {
        val state = _state.value
        val request = state.request ?: return
        _state.update { it.copy(isResubmitting = true, attachmentError = null) }
        viewModelScope.launch {
            resubmitRequest(request, state.type, state.attachments)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isResubmitting = false,
                            resultMessage = "Solicitud reenviada a ${RequestFormatters.shortName(request.approverName)}."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isResubmitting = false,
                            snackbarMessage = exception.requestMessage(RequestAction.RESUBMIT, "No se pudo reenviar la solicitud.")
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }
}
