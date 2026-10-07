package com.performily.flowboard.features.attendance.presentation.state

import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.entity.AttendanceAreaReport
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import java.time.LocalDate

private fun currentMonthAreaPeriod(): AttendancePeriod {
    val today = LocalDate.now()
    return AttendancePeriod(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
}

data class AttendanceAreaUiState(
    val period: AttendancePeriod = currentMonthAreaPeriod(),
    val areas: List<AttendanceArea> = emptyList(),
    val selectedArea: AttendanceArea? = null,
    val statusFilter: String = "TODOS",
    val report: AttendanceAreaReport? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
