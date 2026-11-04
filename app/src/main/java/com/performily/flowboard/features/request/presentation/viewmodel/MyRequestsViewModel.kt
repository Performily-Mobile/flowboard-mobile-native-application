package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.request.application.usecase.GetMyRequestsUseCase
import com.performily.flowboard.features.request.presentation.state.MyRequestsFilter
import com.performily.flowboard.features.request.presentation.state.MyRequestsUiState
import com.performily.flowboard.features.request.presentation.state.PagingState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the requests of the current employee for the "Mis solicitudes" screen.
 */
@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val getMyRequests: GetMyRequestsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyRequestsUiState())
    val state: StateFlow<MyRequestsUiState> = _state.asStateFlow()

    /** Loads the first page of the requests with the selected filter. */
    fun load() {
        val filter = _state.value.filter
        _state.update { it.copy(isLoading = it.requests.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            getMyRequests(filter.status)
                .onSuccess { result ->
                    // Ignore the answer if the filter changed while it was loading.
                    if (_state.value.filter == filter) {
                        _state.update { it.copy(isLoading = false, requests = result.items, paging = PagingState.after(result)) }
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.requestMessage(RequestAction.LOAD, "No se pudieron cargar tus solicitudes.")
                        )
                    }
                }
        }
    }

    /** Shows the message left by another screen and reloads the list. */
    fun onResult(message: String) {
        _state.update { it.copy(snackbarMessage = message) }
        load()
    }

    fun onFilterSelected(filter: MyRequestsFilter) {
        if (filter == _state.value.filter) return
        _state.update { it.copy(filter = filter, requests = emptyList(), paging = PagingState()) }
        load()
    }

    /** Loads the next page and adds it at the end of the list. */
    fun loadMore() {
        val current = _state.value
        if (!current.paging.canLoadMore) return
        val filter = current.filter
        _state.update { it.copy(paging = it.paging.copy(isLoadingMore = true, loadMoreFailed = false)) }
        viewModelScope.launch {
            getMyRequests(filter.status, PagingState.nextPage(current.requests.size))
                .onSuccess { result ->
                    if (_state.value.filter == filter) {
                        _state.update {
                            it.copy(requests = PagingState.merge(it.requests, result), paging = PagingState.after(result))
                        }
                    }
                }
                .onFailure {
                    if (_state.value.filter == filter) {
                        _state.update { it.copy(paging = it.paging.copy(isLoadingMore = false, loadMoreFailed = true)) }
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }
}
