package com.performily.flowboard.features.attendance.presentation.state

import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.entity.AttendanceHoursReport
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import java.time.LocalDate

private fun currentHoursPeriod(): AttendancePeriod {
    val today = LocalDate.now()
    return AttendancePeriod(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
}

data class AttendanceHoursUiState(
    val period: AttendancePeriod = currentHoursPeriod(),
    val areas: List<AttendanceArea> = emptyList(),
    val selectedArea: AttendanceArea? = null,
    val orderByOvertime: Boolean = true,
    val report: AttendanceHoursReport? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
