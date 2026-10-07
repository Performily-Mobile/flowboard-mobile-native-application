package com.performily.flowboard.features.benefits.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.benefits.application.usecase.GetMyBenefitsUseCase
import com.performily.flowboard.features.benefits.presentation.state.MyBenefitsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** MA-58 / MA-59 · Beneficios vigentes y entregados del colaborador (US40). */
@HiltViewModel
class MyBenefitsViewModel @Inject constructor(
    private val getMyBenefits: GetMyBenefitsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyBenefitsUiState())
    val state: StateFlow<MyBenefitsUiState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = it.benefits == null, errorMessage = null) }
        viewModelScope.launch {
            getMyBenefits()
                .onSuccess { benefits -> _state.update { it.copy(isLoading = false, benefits = benefits) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.benefitsMessage(BenefitsAction.LOAD, "No se pudieron cargar tus beneficios.")
                        )
                    }
                }
        }
    }

    fun onTabSelected(index: Int) = _state.update { it.copy(selectedTab = index) }
}
