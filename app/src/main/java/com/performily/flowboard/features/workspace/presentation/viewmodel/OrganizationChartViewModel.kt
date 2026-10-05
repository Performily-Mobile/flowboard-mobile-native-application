package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        viewModelScope.launch {
            getAreas(onlyActive = true).onSuccess { areas -> _state.update { it.copy(areas = areas) } }
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
