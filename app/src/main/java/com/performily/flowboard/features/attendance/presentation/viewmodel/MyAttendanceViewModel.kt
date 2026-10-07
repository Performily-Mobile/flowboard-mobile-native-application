package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.attendance.application.usecase.GetMyAttendanceUseCase
import com.performily.flowboard.features.attendance.application.usecase.RegisterPunchUseCase
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.domain.valueobject.PunchType
import com.performily.flowboard.features.attendance.presentation.state.MyAttendanceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MyAttendanceViewModel @Inject constructor(
    private val getMyAttendance: GetMyAttendanceUseCase,
    private val registerPunch: RegisterPunchUseCase,
    private val currentEmployeeProvider: CurrentEmployeeProvider
) : ViewModel() {

    private val _state = MutableStateFlow(MyAttendanceUiState())
    val state: StateFlow<MyAttendanceUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun setPeriod(from: LocalDate, to: LocalDate) {
        if (to.isBefore(from)) return
        _state.update { it.copy(period = AttendancePeriod(from, to)) }
        load()
    }

    fun setStatusFilter(status: String) {
        _state.update { it.copy(statusFilter = status) }
    }

    fun clearFilters() {
        val today = LocalDate.now()
        _state.update {
            it.copy(
                period = AttendancePeriod(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())),
                statusFilter = "TODOS"
            )
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            val period = _state.value.period
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getMyAttendance(period)
                .onSuccess { records ->
                    _state.update { it.copy(isLoading = false, records = records.sortedByDescending { record -> record.workDate }) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false, errorMessage = exception.message ?: "No se pudo cargar la asistencia.") }
                }
        }
    }

    fun punch() {
        val record = _state.value.todayRecord
        val type = when {
            record == null -> PunchType.CHECK_IN
            record.checkInTime != null && record.checkOutTime == null -> PunchType.CHECK_OUT
            else -> return
        }

        viewModelScope.launch {
            _state.update { it.copy(isPunching = true, actionMessage = null, errorMessage = null) }
            registerPunch(type)
                .onSuccess {
                    _state.update { it.copy(isPunching = false, actionMessage = if (type == PunchType.CHECK_IN) "Entrada registrada." else "Salida registrada.") }
                    load()
                }
                .onFailure { exception ->
                    _state.update { it.copy(isPunching = false, errorMessage = exception.message ?: "No se pudo registrar la marcación.") }
                }
        }
    }
}
