package com.performily.flowboard.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.dashboard.application.usecase.GetHrDashboardUseCase
import com.performily.flowboard.features.dashboard.presentation.state.HrDashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the state of the HR dashboard and reloads it on demand. */
@HiltViewModel
class HrDashboardViewModel @Inject constructor(
    private val getHrDashboard: GetHrDashboardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HrDashboardUiState())
    val state: StateFlow<HrDashboardUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    /**
     * Loads the dashboard when the tab is entered.
     *
     * The first time it shows the loading indicator; afterwards it updates without blocking the screen.
     */
    fun load() = fetch(isRefresh = false)

    /** Reloads the dashboard after a pull-to-refresh or a retry. */
    fun refresh() = fetch(isRefresh = true)

    /**
     * Fetches the dashboard unless a request is already running.
     *
     * @param isRefresh true when the user asked for the reload explicitly.
     */
    private fun fetch(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh && it.dashboard == null,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }
            val dashboard = getHrDashboard()
            _state.update {
                it.copy(
                    dashboard = dashboard,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = if (dashboard.hasNoData) {
                        "No se pudo cargar el panel. Revisa tu conexión e inténtalo de nuevo."
                    } else {
                        null
                    }
                )
            }
        }
    }
}
