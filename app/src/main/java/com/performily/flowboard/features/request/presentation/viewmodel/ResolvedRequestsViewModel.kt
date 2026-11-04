package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.request.application.usecase.GetResolvedTeamRequestsUseCase
import com.performily.flowboard.features.request.presentation.state.PagingState
import com.performily.flowboard.features.request.presentation.state.ResolvedRequestsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel
class ResolvedRequestsViewModel @Inject constructor(
    private val getResolvedTeamRequests: GetResolvedTeamRequestsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ResolvedRequestsUiState())
    val state: StateFlow<ResolvedRequestsUiState> = _state.asStateFlow()

    /** Loads the first page of the resolved requests. */
    fun load() {
        _state.update { it.copy(isLoading = it.requests.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            getResolvedTeamRequests()
                .onSuccess { result ->
                    _state.update { it.copy(isLoading = false, requests = result.items, paging = PagingState.after(result)) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.requestMessage(RequestAction.LOAD, "No se pudieron cargar las solicitudes resueltas.")
                        )
                    }
                }
        }
    }

    /** Loads the next page and adds it at the end of the list. */
    fun loadMore() {
        val current = _state.value
        if (!current.paging.canLoadMore) return
        _state.update { it.copy(paging = it.paging.copy(isLoadingMore = true, loadMoreFailed = false)) }
        viewModelScope.launch {
            getResolvedTeamRequests(PagingState.nextPage(current.requests.size))
                .onSuccess { result ->
                    _state.update {
                        it.copy(requests = PagingState.merge(it.requests, result), paging = PagingState.after(result))
                    }
                }
                .onFailure {
                    _state.update { it.copy(paging = it.paging.copy(isLoadingMore = false, loadMoreFailed = true)) }
                }
        }
    }
}
