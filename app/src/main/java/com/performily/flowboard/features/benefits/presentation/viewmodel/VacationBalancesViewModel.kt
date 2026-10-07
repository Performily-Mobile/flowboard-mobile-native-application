package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.GetVacationBalancesUseCase
import com.performily.flowboard.features.benefits.presentation.state.VacationBalancesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Saldos de vacaciones de los colaboradores activos, para elegir a quién ajustar (MA-63). */
@HiltViewModel
class VacationBalancesViewModel @Inject constructor(
    private val getVacationBalances: GetVacationBalancesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VacationBalancesUiState())
    val state: StateFlow<VacationBalancesUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = it.balances.isEmpty(), errorMessage = null) }
        viewModelScope.launch {
            getVacationBalances()
                .onSuccess { balances -> _state.update { it.copy(isLoading = false, balances = balances) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudieron cargar los saldos.")
                        )
                    }
                }
        }
    }

    fun onQueryChange(value: String) = _state.update { it.copy(query = value) }
}
