package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.request.application.usecase.ApproveRequestUseCase
import com.performily.flowboard.features.request.application.usecase.GetAllRequestsUseCase
import com.performily.flowboard.features.request.application.usecase.GetMyRequestsUseCase
import com.performily.flowboard.features.request.application.usecase.GetPendingApprovalsUseCase
import com.performily.flowboard.features.request.application.usecase.GetRequestTypesUseCase
import com.performily.flowboard.features.request.application.usecase.RejectRequestUseCase
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.presentation.state.AllRequestsFilter
import com.performily.flowboard.features.request.presentation.state.ApprovalSort
import com.performily.flowboard.features.request.presentation.state.MyRequestsFilter
import com.performily.flowboard.features.request.presentation.state.RejectForm
import com.performily.flowboard.features.request.presentation.state.RequestsTab
import com.performily.flowboard.features.request.presentation.state.RequestsUiState
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class RequestsViewModel @Inject constructor(
    currentEmployeeProvider: CurrentEmployeeProvider,
    private val getMyRequests: GetMyRequestsUseCase,
    private val getPendingApprovals: GetPendingApprovalsUseCase,
    private val getAllRequests: GetAllRequestsUseCase,
    private val getRequestTypes: GetRequestTypesUseCase,
    private val approveRequest: ApproveRequestUseCase,
    private val rejectRequest: RejectRequestUseCase
) : ViewModel() {

    private val role = currentEmployeeProvider.currentRole()

    private val _state = MutableStateFlow(
        RequestsUiState(role = role, selectedTab = RequestsTab.forRole(role).first())
    )
    val state: StateFlow<RequestsUiState> = _state.asStateFlow()


    fun load() {
        if (role == UserRole.EMPLOYEE) loadMine()
        loadPending()
        loadTypes()
        if (_state.value.selectedTab == RequestsTab.ALL) loadAll()
    }

    fun onTabSelected(tab: RequestsTab) {
        _state.update { it.copy(selectedTab = tab) }
        if (tab == RequestsTab.ALL && _state.value.allRequests.isEmpty()) loadAll()
    }

    /** Al volver de otra pantalla con un mensaje (solicitud enviada, aprobada...). */
    fun onResult(message: String) {
        _state.update { it.copy(snackbarMessage = message) }
        load()
    }


    private fun loadMine() {
        _state.update { it.copy(isLoadingMine = it.myRequests.isEmpty(), mineError = null) }
        viewModelScope.launch {
            getMyRequests()
                .onSuccess { list -> _state.update { it.copy(isLoadingMine = false, myRequests = list) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoadingMine = false,
                            mineError = exception.requestMessage(RequestAction.LOAD, "No se pudieron cargar tus solicitudes.")
                        )
                    }
                }
        }
    }

    fun onMyFilterSelected(filter: MyRequestsFilter) = _state.update { it.copy(myFilter = filter) }

    private fun loadPending() {
        val typeId = _state.value.typeFilter?.id
        _state.update { it.copy(isLoadingPending = it.pending.isEmpty(), pendingError = null) }
        viewModelScope.launch {
            getPendingApprovals(typeId)
                .onSuccess { list ->
                    // Si el filtro cambió mientras cargaba, se ignora esta respuesta.
                    if (_state.value.typeFilter?.id == typeId) {
                        _state.update { it.copy(isLoadingPending = false, pending = list) }
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoadingPending = false,
                            pendingError = exception.requestMessage(RequestAction.LOAD, "No se pudo cargar la bandeja.")
                        )
                    }
                }
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            getRequestTypes(activeOnly = false).onSuccess { types -> _state.update { it.copy(requestTypes = types) } }
        }
    }

    fun onTypeFilterSelected(type: RequestType?) {
        if (type?.id == _state.value.typeFilter?.id) return
        _state.update { it.copy(typeFilter = type, pending = emptyList()) }
        loadPending()
    }

    fun onSortSelected(sort: ApprovalSort) = _state.update { it.copy(sort = sort) }

    fun approve(request: Request) {
        if (_state.value.processingRequestId != null) return
        _state.update { it.copy(processingRequestId = request.id) }
        viewModelScope.launch {
            approveRequest(request)
                .onSuccess { approved ->
                    _state.update { state ->
                        state.copy(
                            processingRequestId = null,
                            pending = state.pending.filterNot { it.id == approved.id },
                            snackbarMessage = "Solicitud de ${RequestFormatters.shortName(request.requesterName)} aprobada."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            processingRequestId = null,
                            snackbarMessage = exception.requestMessage(RequestAction.RESOLVE, "No se pudo aprobar la solicitud.")
                        )
                    }
                }
        }
    }

    fun showRejectDialog(request: Request) = _state.update { it.copy(rejectForm = RejectForm(request)) }

    fun dismissRejectDialog() = _state.update { it.copy(rejectForm = null) }

    fun onRejectReasonChange(value: String) =
        _state.update { it.copy(rejectForm = it.rejectForm?.copy(reason = value, error = null)) }

    fun confirmReject() {
        val form = _state.value.rejectForm ?: return
        if (form.reason.isBlank()) {
            _state.update { it.copy(rejectForm = form.copy(error = "Ingresa el motivo para rechazar la solicitud.")) }
            return
        }
        _state.update { it.copy(rejectForm = form.copy(isSaving = true)) }
        viewModelScope.launch {
            rejectRequest(form.request, form.reason)
                .onSuccess { rejected ->
                    _state.update { state ->
                        state.copy(
                            rejectForm = null,
                            pending = state.pending.filterNot { it.id == rejected.id },
                            snackbarMessage = "Solicitud de ${RequestFormatters.shortName(form.request.requesterName)} rechazada."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            rejectForm = it.rejectForm?.copy(
                                isSaving = false,
                                error = exception.requestMessage(RequestAction.RESOLVE, "No se pudo rechazar la solicitud.")
                            )
                        )
                    }
                }
        }
    }

    private fun loadAll() {
        _state.update { it.copy(isLoadingAll = it.allRequests.isEmpty(), allError = null) }
        viewModelScope.launch {
            getAllRequests()
                .onSuccess { list -> _state.update { it.copy(isLoadingAll = false, allRequests = list) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoadingAll = false,
                            allError = exception.requestMessage(RequestAction.LOAD, "No se pudieron cargar las solicitudes.")
                        )
                    }
                }
        }
    }

    fun onAllFilterSelected(filter: AllRequestsFilter) = _state.update { it.copy(allFilter = filter) }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }
}
