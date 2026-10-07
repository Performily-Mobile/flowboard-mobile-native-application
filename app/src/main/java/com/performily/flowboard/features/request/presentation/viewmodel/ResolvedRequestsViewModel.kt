package com.performily.flowboard.features.request.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.request.application.usecase.GetResolvedTeamRequestsUseCase
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

    fun load() {
        _state.update { it.copy(isLoading = it.requests.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            getResolvedTeamRequests()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, requests = list) } }
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
}
