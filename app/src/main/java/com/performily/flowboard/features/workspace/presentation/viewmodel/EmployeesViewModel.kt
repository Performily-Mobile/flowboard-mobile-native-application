package com.performily.flowboard.features.workspace.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetEmployeesUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetPositionsUseCase
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.features.workspace.presentation.state.EmployeesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeesViewModel @Inject constructor(
    private val getEmployees: GetEmployeesUseCase,
    private val getAreas: GetAreasUseCase,
    private val getPositions: GetPositionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EmployeesUiState())
    val state: StateFlow<EmployeesUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadFilterOptions()
    }

    fun loadEmployees() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { fetchEmployees() }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            fetchEmployees()
        }
    }

    fun onAreaSelected(area: Area?) {
        _state.update { current ->
            val keepPosition = area == null || current.selectedPosition?.belongsTo(area.id) == true
            current.copy(
                selectedArea = area,
                selectedPosition = if (keepPosition) current.selectedPosition else null
            )
        }
        loadEmployees()
    }

    fun onStatusSelected(status: EmploymentStatus?) {
        _state.update { it.copy(selectedStatus = status) }
        loadEmployees()
    }

    fun onPositionSelected(position: Position?) {
        _state.update { it.copy(selectedPosition = position) }
        loadEmployees()
    }

    fun clearFilters() {
        _state.update {
            it.copy(query = "", selectedArea = null, selectedStatus = null, selectedPosition = null)
        }
        loadEmployees()
    }

    private suspend fun fetchEmployees() {
        val current = _state.value
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        getEmployees(
            search = current.query,
            areaId = current.selectedArea?.id,
            status = current.selectedStatus,
            positionId = current.selectedPosition?.id
        )
            .onSuccess { employees ->
                _state.update { it.copy(isLoading = false, employees = employees) }
            }
            .onFailure { exception ->
                _state.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            getAreas(onlyActive = true).onSuccess { areas -> _state.update { it.copy(areas = areas) } }
            getPositions(onlyActive = true).onSuccess { positions -> _state.update { it.copy(positions = positions) } }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L
    }
}
