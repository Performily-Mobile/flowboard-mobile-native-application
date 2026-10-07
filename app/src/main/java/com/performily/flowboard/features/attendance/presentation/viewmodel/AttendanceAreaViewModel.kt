package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.attendance.application.usecase.GetAreaAttendanceUseCase
import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.presentation.state.AttendanceAreaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceAreaViewModel @Inject constructor(
    private val getAreaAttendance: GetAreaAttendanceUseCase,
    private val repository: com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceAreaUiState())
    val state: StateFlow<AttendanceAreaUiState> = _state.asStateFlow()

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

    fun setStatusFilter(status: String) {
        _state.update { it.copy(statusFilter = status) }
    }

    fun load() {
        val area = _state.value.selectedArea ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getAreaAttendance(area.id, _state.value.period)
                .onSuccess { report ->
                    _state.update { it.copy(isLoading = false, report = report.copy(areaName = area.name)) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message ?: "No se pudo cargar el reporte.") }
                }
        }
    }

    private fun loadAreas() {
        viewModelScope.launch {
            repository.getAreas()
                .onSuccess { areas ->
                    _state.update { current ->
                        current.copy(
                            areas = areas,
                            selectedArea = current.selectedArea ?: areas.firstOrNull()
                        )
                    }
                    load()
                }
                .onFailure { exception ->
                    _state.update { it.copy(errorMessage = exception.message ?: "No se pudieron cargar las áreas.") }
                }
        }
    }
}