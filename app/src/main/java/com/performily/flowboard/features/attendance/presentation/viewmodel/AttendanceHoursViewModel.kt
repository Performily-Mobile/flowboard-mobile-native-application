package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.attendance.application.usecase.GetAttendanceHoursUseCase
import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.presentation.state.AttendanceHoursUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceHoursViewModel @Inject constructor(
    private val getAttendanceHours: GetAttendanceHoursUseCase,
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceHoursUiState())
    val state: StateFlow<AttendanceHoursUiState> = _state.asStateFlow()

    init {
        loadAreas()
    }

    fun setPeriod(from: LocalDate, to: LocalDate) {
        if (to.isBefore(from)) return
        _state.update { it.copy(period = AttendancePeriod(from, to)) }
        load()
    }

    fun selectArea(area: AttendanceArea?) {
        _state.update { it.copy(selectedArea = area) }
        load()
    }

    fun toggleOrder() {
        _state.update { it.copy(orderByOvertime = !it.orderByOvertime) }
        load()
    }

    fun load() {
        val areaId = _state.value.selectedArea?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getAttendanceHours(_state.value.period, areaId, _state.value.orderByOvertime)
                .onSuccess { report -> _state.update { it.copy(isLoading = false, report = report) } }
                .onFailure { exception -> _state.update { it.copy(isLoading = false, errorMessage = exception.message ?: "No se pudo cargar el reporte de horas.") } }
        }
    }

    private fun loadAreas() {
        viewModelScope.launch {
            repository.getAreas()
                .onSuccess { areas ->
                    _state.update { current -> current.copy(areas = areas, selectedArea = current.selectedArea ?: areas.firstOrNull()) }
                    load()
                }
                .onFailure { exception -> _state.update { it.copy(errorMessage = exception.message ?: "No se pudieron cargar las áreas.") } }
        }
    }
}
