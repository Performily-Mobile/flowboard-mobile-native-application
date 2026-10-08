package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.wellbeing.application.usecase.GetOfficesUseCase
import com.performily.flowboard.features.wellbeing.presentation.state.OfficesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** MA-70 - List of offices with their overall indicator (US47, US50). */
@HiltViewModel
class OfficesViewModel @Inject constructor(
    private val getOffices: GetOfficesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OfficesUiState())
    val state: StateFlow<OfficesUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    /**
     * Loads the offices.
     *
     * The screen calls this on entry and periodically. The loading indicator is shown only
     * before the first successful response; reloads update the list silently and never replace
     * an error screen with a spinner.
     */
    fun load() = fetch(manual = false)

    /** Reloads after the user taps "Reintentar", showing the loading indicator again. */
    fun retry() = fetch(manual = true)

    /**
     * Starts a load, cancelling any request still in flight so an old response cannot overwrite a newer one.
     *
     * @param manual true when the user asked for the reload explicitly
     */
    private fun fetch(manual: Boolean) {
        loadJob?.cancel()
        _state.update {
            it.copy(
                isLoading = !it.hasLoaded && (manual || it.errorMessage == null),
                errorMessage = if (manual) null else it.errorMessage
            )
        }
        loadJob = viewModelScope.launch {
            getOffices()
                .onSuccess { offices ->
                    _state.update { it.copy(isLoading = false, hasLoaded = true, offices = offices, errorMessage = null) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.wellbeingMessage("No se pudieron cargar los espacios."))
                    }
                }
        }
    }
}
