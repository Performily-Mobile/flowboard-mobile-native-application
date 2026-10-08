package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.request.application.usecase.ApproveRequestUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestTypesUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestUseCase
import com.performily.flowboard.features.request.application.usecase.GetVacationAvailabilityUseCase
import com.performily.flowboard.features.request.application.usecase.RejectRequestUseCase
import com.performily.flowboard.features.request.application.usecase.ReturnRequestForReviewUseCase
import com.performily.flowboard.features.request.presentation.state.ReviewRequestUiState
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
class ReviewRequestViewModel @Inject constructor(
    private val currentEmployee: CurrentEmployeeProvider,
    private val getRequest: GetRequestUseCase,
    private val getRequestTypes: GetRequestTypesUseCase,
    private val getVacationAvailability: GetVacationAvailabilityUseCase,
    private val approveRequest: ApproveRequestUseCase,
    private val rejectRequest: RejectRequestUseCase,
    private val returnRequestForReview: ReturnRequestForReviewUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewRequestUiState())
    val state: StateFlow<ReviewRequestUiState> = _state.asStateFlow()

    fun load(requestId: Long) {
        if (_state.value.requestId == requestId && _state.value.request != null) return
        _state.update {
            ReviewRequestUiState(
                requestId = requestId,
                viewerId = currentEmployee.currentEmployeeId().value,
                role = currentEmployee.currentRole(),
                isLoading = true
            )
        }
        viewModelScope.launch {
            val types = async { getRequestTypes().getOrNull().orEmpty() }
            getRequest(requestId)
                .onSuccess { request ->
                    val type = types.await().firstOrNull { it.id == request.requestTypeId }
                    _state.update { it.copy(isLoading = false, request = request, type = type) }
                    // "14 días → 9 tras aprobar": solo si el tipo descuenta vacaciones.
                    if (type?.deductsVacationDays == true && request.isPending) {
                        getVacationAvailability(request.requesterId)
                            .onSuccess { availability -> _state.update { it.copy(availability = availability) } }
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

    fun approve() {
        val request = _state.value.request ?: return
        _state.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            approveRequest(request)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            resultMessage = "Solicitud de ${RequestFormatters.shortName(request.requesterName)} aprobada."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            snackbarMessage = exception.requestMessage(RequestAction.RESOLVE, "No se pudo aprobar la solicitud.")
                        )
                    }
                }
        }
    }


    fun showRejectDialog() = _state.update { it.copy(isRejectDialogVisible = true, rejectReason = "", rejectError = null) }

    fun dismissRejectDialog() = _state.update { it.copy(isRejectDialogVisible = false) }

    fun onRejectReasonChange(value: String) = _state.update { it.copy(rejectReason = value, rejectError = null) }

    fun confirmReject() {
        val state = _state.value
        val request = state.request ?: return
        if (state.rejectReason.isBlank()) {
            _state.update { it.copy(rejectError = "Ingresa el motivo para rechazar la solicitud.") }
            return
        }
        _state.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            rejectRequest(request, state.rejectReason)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            isRejectDialogVisible = false,
                            resultMessage = "Solicitud de ${RequestFormatters.shortName(request.requesterName)} rechazada."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            rejectError = exception.requestMessage(RequestAction.RESOLVE, "No se pudo rechazar la solicitud.")
                        )
                    }
                }
        }
    }


    fun showReturnSheet() = _state.update { it.copy(isReturnSheetVisible = true, returnComment = "", returnError = null) }

    fun dismissReturnSheet() = _state.update { it.copy(isReturnSheetVisible = false) }

    fun onReturnCommentChange(value: String) = _state.update { it.copy(returnComment = value, returnError = null) }

    fun confirmReturn() {
        val state = _state.value
        val request = state.request ?: return
        if (state.returnComment.isBlank()) {
            _state.update { it.copy(returnError = "Ingresa un comentario para el colaborador.") }
            return
        }
        _state.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            returnRequestForReview(request, state.returnComment)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            isReturnSheetVisible = false,
                            resultMessage = "Solicitud devuelta a ${RequestFormatters.shortName(request.requesterName)} para revisión."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            returnError = exception.requestMessage(RequestAction.RESOLVE, "No se pudo devolver la solicitud.")
                        )
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }
}
