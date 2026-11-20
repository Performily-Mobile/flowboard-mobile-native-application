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
import com.performily.flowboard.features.request.presentation.state.PagingState
import com.performily.flowboard.features.request.presentation.state.QueuedApproval
import com.performily.flowboard.features.request.presentation.state.RejectForm
import com.performily.flowboard.features.request.presentation.state.RequestsTab
import com.performily.flowboard.features.request.presentation.state.RequestsUiState
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
        RequestsUiState(
            role = role,
            viewerId = currentEmployeeProvider.currentEmployeeId().value,
            selectedTab = RequestsTab.forRole(role).first()
        )
    )
    val state: StateFlow<RequestsUiState> = _state.asStateFlow()

    private var approvalJob: Job? = null


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
        val filter = _state.value.myFilter
        _state.update { it.copy(isLoadingMine = it.myRequests.isEmpty(), mineError = null) }
        viewModelScope.launch {
            getMyRequests(filter.status)
                .onSuccess { result ->
                    // Ignore the answer if the filter changed while it was loading.
                    if (_state.value.myFilter == filter) {
                        _state.update {
                            it.copy(isLoadingMine = false, myRequests = result.items, minePaging = PagingState.after(result))
                        }
                    }
                }
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

    fun onMyFilterSelected(filter: MyRequestsFilter) {
        if (filter == _state.value.myFilter) return
        _state.update { it.copy(myFilter = filter, myRequests = emptyList(), minePaging = PagingState()) }
        loadMine()
    }

    /** Loads the next page of the viewer's requests and adds it at the end of the list. */
    fun loadMoreMine() {
        val current = _state.value
        if (!current.minePaging.canLoadMore) return
        val filter = current.myFilter
        _state.update { it.copy(minePaging = it.minePaging.copy(isLoadingMore = true, loadMoreFailed = false)) }
        viewModelScope.launch {
            getMyRequests(filter.status, PagingState.nextPage(current.myRequests.size))
                .onSuccess { result ->
                    if (_state.value.myFilter == filter) {
                        _state.update {
                            it.copy(myRequests = PagingState.merge(it.myRequests, result), minePaging = PagingState.after(result))
                        }
                    }
                }
                .onFailure {
                    if (_state.value.myFilter == filter) {
                        _state.update { it.copy(minePaging = it.minePaging.copy(isLoadingMore = false, loadMoreFailed = true)) }
                    }
                }
        }
    }

    private fun loadPending() {
        val typeId = _state.value.typeFilter?.id
        val sort = _state.value.sort
        _state.update { it.copy(isLoadingPending = it.pending.isEmpty(), pendingError = null) }
        viewModelScope.launch {
            getPendingApprovals(typeId, sort.order)
                .onSuccess { result ->
                    // Ignore the answer if the filter or the order changed while it was loading.
                    if (_state.value.typeFilter?.id == typeId && _state.value.sort == sort) {
                        _state.update {
                            it.copy(
                                isLoadingPending = false,
                                pending = result.items,
                                pendingTotal = result.totalItems,
                                pendingPaging = PagingState.after(result)
                            )
                        }
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
        _state.update { it.copy(typeFilter = type, pending = emptyList(), pendingPaging = PagingState()) }
        loadPending()
    }

    fun onSortSelected(sort: ApprovalSort) {
        if (sort == _state.value.sort) return
        _state.update { it.copy(sort = sort, pending = emptyList(), pendingPaging = PagingState()) }
        loadPending()
    }

    /** Loads the next page of the pending requests and adds it at the end of the list. */
    fun loadMorePending() {
        val current = _state.value
        if (!current.pendingPaging.canLoadMore) return
        val typeId = current.typeFilter?.id
        val sort = current.sort
        _state.update { it.copy(pendingPaging = it.pendingPaging.copy(isLoadingMore = true, loadMoreFailed = false)) }
        viewModelScope.launch {
            getPendingApprovals(typeId, sort.order, PagingState.nextPage(current.pending.size))
                .onSuccess { result ->
                    if (_state.value.typeFilter?.id == typeId && _state.value.sort == sort) {
                        _state.update {
                            it.copy(
                                pending = PagingState.merge(it.pending, result),
                                pendingTotal = result.totalItems,
                                pendingPaging = PagingState.after(result)
                            )
                        }
                    }
                }
                .onFailure {
                    if (_state.value.typeFilter?.id == typeId && _state.value.sort == sort) {
                        _state.update {
                            it.copy(pendingPaging = it.pendingPaging.copy(isLoadingMore = false, loadMoreFailed = true))
                        }
                    }
                }
        }
    }

    /**
     * Approves a request with a short undo window.
     *
     * The request leaves the list at once and the snackbar offers "Deshacer"; the approval is sent
     * only when [UNDO_WINDOW_MILLIS] pass without an undo. A second approval sends the first one
     * right away, so only one approval waits at a time.
     *
     * @param request request to approve.
     */
    fun approve(request: Request) {
        val current = _state.value
        if (current.processingRequestId != null || !current.canResolve(request)) return
        current.queuedApproval?.let { commitQueuedApproval() }
        val position = _state.value.pending.indexOfFirst { it.id == request.id }.coerceAtLeast(0)
        _state.update { state ->
            state.copy(
                pending = state.pending.filterNot { it.id == request.id },
                pendingTotal = (state.pendingTotal - 1).coerceAtLeast(0),
                queuedApproval = QueuedApproval(request, position)
            )
        }
        approvalJob = viewModelScope.launch {
            delay(UNDO_WINDOW_MILLIS)
            commitQueuedApproval()
        }
    }

    /** Puts the queued request back in its place and cancels its approval. */
    fun undoApproval() {
        approvalJob?.cancel()
        val queued = _state.value.queuedApproval ?: return
        _state.update { state ->
            val list = state.pending.toMutableList()
            list.add(queued.position.coerceAtMost(list.size), queued.request)
            state.copy(pending = list, pendingTotal = state.pendingTotal + 1, queuedApproval = null)
        }
    }

    /** Sends the queued approval to the backend; a failure puts the request back with a message. */
    private fun commitQueuedApproval() {
        approvalJob?.cancel()
        val queued = _state.value.queuedApproval ?: return
        _state.update { it.copy(queuedApproval = null, processingRequestId = queued.request.id) }
        viewModelScope.launch {
            approveRequest(queued.request)
                .onSuccess { _state.update { it.copy(processingRequestId = null) } }
                .onFailure { exception ->
                    _state.update { state ->
                        val list = state.pending.toMutableList()
                        list.add(queued.position.coerceAtMost(list.size), queued.request)
                        state.copy(
                            processingRequestId = null,
                            pending = list,
                            pendingTotal = state.pendingTotal + 1,
                            snackbarMessage = exception.requestMessage(RequestAction.RESOLVE, "No se pudo aprobar la solicitud.")
                        )
                    }
                }
        }
    }

    /** Sends a waiting approval when the screen goes away for good, so no decision is lost. */
    override fun onCleared() {
        val queued = _state.value.queuedApproval
        if (queued != null) {
            approvalJob?.cancel()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { approveRequest(queued.request) }
        }
        super.onCleared()
    }

    fun showRejectDialog(request: Request) {
        if (!_state.value.canResolve(request)) return
        _state.update { it.copy(rejectForm = RejectForm(request)) }
    }

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
                            pendingTotal = (state.pendingTotal - 1).coerceAtLeast(0),
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
        val filter = _state.value.allFilter
        _state.update { it.copy(isLoadingAll = it.allRequests.isEmpty(), allError = null) }
        viewModelScope.launch {
            getAllRequests(filter.status)
                .onSuccess { result ->
                    // Ignore the answer if the filter changed while it was loading.
                    if (_state.value.allFilter == filter) {
                        _state.update {
                            it.copy(isLoadingAll = false, allRequests = result.items, allPaging = PagingState.after(result))
                        }
                    }
                }
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

    fun onAllFilterSelected(filter: AllRequestsFilter) {
        if (filter == _state.value.allFilter) return
        _state.update { it.copy(allFilter = filter, allRequests = emptyList(), allPaging = PagingState()) }
        loadAll()
    }

    /** Loads the next page of all the requests and adds it at the end of the list. */
    fun loadMoreAll() {
        val current = _state.value
        if (!current.allPaging.canLoadMore) return
        val filter = current.allFilter
        _state.update { it.copy(allPaging = it.allPaging.copy(isLoadingMore = true, loadMoreFailed = false)) }
        viewModelScope.launch {
            getAllRequests(filter.status, PagingState.nextPage(current.allRequests.size))
                .onSuccess { result ->
                    if (_state.value.allFilter == filter) {
                        _state.update {
                            it.copy(allRequests = PagingState.merge(it.allRequests, result), allPaging = PagingState.after(result))
                        }
                    }
                }
                .onFailure {
                    if (_state.value.allFilter == filter) {
                        _state.update { it.copy(allPaging = it.allPaging.copy(isLoadingMore = false, loadMoreFailed = true)) }
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }

    private companion object {
        /** Time the user has to undo an approval before it is sent. */
        const val UNDO_WINDOW_MILLIS = 4_000L
    }
}
