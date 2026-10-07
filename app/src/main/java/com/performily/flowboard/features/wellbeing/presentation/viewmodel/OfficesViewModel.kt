package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.wellbeing.application.usecase.GetOfficesUseCase
import com.performily.flowboard.features.wellbeing.presentation.state.OfficesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** MA-70 · Lista de espacios con su indicador general (US47, US50). */
@HiltViewModel
class OfficesViewModel @Inject constructor(
    private val getOffices: GetOfficesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OfficesUiState())
    val state: StateFlow<OfficesUiState> = _state.asStateFlow()

    /**
     * La pantalla llama a esto al entrar y cada cierto tiempo. Solo muestra el
     * indicador de carga la primera vez; las recargas actualizan la lista en silencio.
     */
    fun load() {
        _state.update { it.copy(isLoading = !it.hasLoaded, errorMessage = null) }
        viewModelScope.launch {
            getOffices()
                .onSuccess { offices -> _state.update { it.copy(isLoading = false, hasLoaded = true, offices = offices) } }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.wellbeingMessage("No se pudieron cargar los espacios."))
                    }
                }
        }
    }
}
