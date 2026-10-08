package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.GetMyVacationBalanceUseCase
import com.performily.flowboard.features.benefits.presentation.state.MyVacationBalanceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MA-57 · Saldo de vacaciones del colaborador (US41). Sin conexión muestra la
 * última copia guardada con el aviso de cuándo se sincronizó.
 */
@HiltViewModel
class MyVacationBalanceViewModel @Inject constructor(
    private val getMyVacationBalance: GetMyVacationBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyVacationBalanceUiState())
    val state: StateFlow<MyVacationBalanceUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = it.synced == null, errorMessage = null) }
        viewModelScope.launch {
            getMyVacationBalance()
                .onSuccess { synced -> _state.update { it.copy(isLoading = false, synced = synced) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudo cargar tu saldo de vacaciones.")
                        )
                    }
                }
        }
    }
}
