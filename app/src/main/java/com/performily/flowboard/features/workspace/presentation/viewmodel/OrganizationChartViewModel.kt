package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetOrganizationChartUseCase
import com.performily.flowboard.features.workspace.presentation.state.OrganizationChartMode
import com.performily.flowboard.features.workspace.presentation.state.OrganizationChartUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrganizationChartViewModel @Inject constructor(
    private val getOrganizationChart: GetOrganizationChartUseCase,
    private val getAreas: GetAreasUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizationChartUiState())
    val state: StateFlow<OrganizationChartUiState> = _state.asStateFlow()

    private var initialized = false

    init {
        viewModelScope.launch {
            getAreas(onlyActive = true).onSuccess { areas -> _state.update { it.copy(areas = areas) } }
        }
    }

    /**
     * Se llama una vez al abrir la pantalla.
     * Si llega un área, abre directo la vista "Por área" (vista del colaborador, MA-17)
     * y resalta al colaborador indicado con "(tú)".
     */
    fun initialize(areaId: Long?, highlightedEmployeeId: Long?) {
        if (initialized) return
        initialized = true
        _state.update {
            it.copy(
                mode = if (areaId != null) OrganizationChartMode.BY_AREA else OrganizationChartMode.WHOLE_ORGANIZATION,
                selectedAreaId = areaId,
                highlightedEmployeeId = highlightedEmployeeId?.let(::EmployeeId)
            )
        }
        loadChart()
    }

    fun onModeChange(mode: OrganizationChartMode) {
        _state.update { current ->
            current.copy(
                mode = mode,
                selectedAreaId = if (mode == OrganizationChartMode.BY_AREA) {
                    current.selectedAreaId ?: current.areas.firstOrNull()?.id
                } else {
                    current.selectedAreaId
                }
            )
        }
        loadChart()
    }

    fun onAreaSelected(areaId: Long) {
        _state.update { it.copy(selectedAreaId = areaId) }
        loadChart()
    }

    fun loadChart() {
        val current = _state.value
        val areaId = if (current.mode == OrganizationChartMode.BY_AREA) current.selectedAreaId else null
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getOrganizationChart(areaId)
                .onSuccess { chart -> _state.update { it.copy(isLoading = false, chart = chart) } }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }
}
