package com.performily.flowboard.features.attendance.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.attendance.application.usecase.JustifyAttendanceUseCase
import com.performily.flowboard.features.attendance.presentation.state.JustifyAttendanceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class JustifyAttendanceViewModel @Inject constructor(
    private val justifyAttendance: JustifyAttendanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        JustifyAttendanceUiState(0L, "")
    )
    val state: StateFlow<JustifyAttendanceUiState> = _state.asStateFlow()

    fun initialize(attendanceRecordId: Long, workDate: LocalDate) {
        if (_state.value.attendanceRecordId == attendanceRecordId) return
        val formatter = DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", Locale("es", "PE"))
        _state.value = JustifyAttendanceUiState(
            attendanceRecordId = attendanceRecordId,
            workDateLabel = workDate.format(formatter).replaceFirstChar { it.uppercase() }
        )
    }

    fun onTypeChange(type: String) = _state.update { it.copy(justificationType = type) }
    fun onReasonChange(reason: String) = _state.update { it.copy(reason = reason) }
    fun onEvidenceSelected(name: String, uri: String) = _state.update { it.copy(evidenceName = name, evidenceUrl = uri, errorMessage = null) }

    fun submit() {
        val current = _state.value
        if (current.attendanceRecordId == 0L || current.reason.isBlank()) {
            _state.update { it.copy(errorMessage = "Completa el motivo de la inasistencia.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            justifyAttendance(
                attendanceRecordId = current.attendanceRecordId,
                reason = "${current.justificationType}: ${current.reason.trim()}",
                evidenceUrl = current.evidenceUrl
            )
                .onSuccess { _state.update { it.copy(isSaving = false, success = true) } }
                .onFailure { exception -> _state.update { it.copy(isSaving = false, errorMessage = exception.message ?: "No se pudo enviar la justificación.") } }
        }
    }
}
