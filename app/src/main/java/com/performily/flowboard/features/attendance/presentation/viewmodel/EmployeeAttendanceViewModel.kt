package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.attendance.application.usecase.GetEmployeeAttendanceUseCase
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.presentation.state.EmployeeAttendanceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EmployeeAttendanceViewModel @Inject constructor(
    private val getEmployeeAttendance: GetEmployeeAttendanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        EmployeeAttendanceUiState(employeeId = 0L, employeeName = "")
    )
    val state: StateFlow<EmployeeAttendanceUiState> = _state.asStateFlow()

    fun initialize(employeeId: Long, employeeName: String) {
        if (_state.value.employeeId == employeeId && _state.value.employeeName == employeeName) return
        _state.value = EmployeeAttendanceUiState(employeeId = employeeId, employeeName = employeeName)
        load()
    }

    fun setPeriod(from: LocalDate, to: LocalDate) {
        if (to.isBefore(from)) return
        _state.update { it.copy(period = AttendancePeriod(from, to)) }
        load()
    }

    fun load() {
        val employeeId = _state.value.employeeId
        if (employeeId == 0L) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getEmployeeAttendance(employeeId, _state.value.period)
                .onSuccess { records ->
                    val resolvedName = records.firstOrNull()?.employeeName ?: _state.value.employeeName
                    _state.update {
                        it.copy(
                            isLoading = false,
                            employeeName = resolvedName,
                            records = records.sortedByDescending { record -> record.workDate }
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message ?: "No se pudo cargar la asistencia.") }
                }
        }
    }
}
