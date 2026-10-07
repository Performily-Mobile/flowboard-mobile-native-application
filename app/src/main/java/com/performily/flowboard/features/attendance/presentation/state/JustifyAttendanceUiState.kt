package com.performily.flowboard.features.attendance.presentation.state

data class JustifyAttendanceUiState(
    val attendanceRecordId: Long,
    val workDateLabel: String,
    val justificationType: String = "Descanso médico",
    val reason: String = "",
    val evidenceName: String? = null,
    val evidenceUrl: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)
