package com.performily.flowboard.features.attendance.presentation.state

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import java.time.LocalDate

private fun currentEmployeePeriod(): AttendancePeriod {
    val today = LocalDate.now()
    return AttendancePeriod(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
}

data class EmployeeAttendanceUiState(
    val employeeId: Long,
    val employeeName: String,
    val period: AttendancePeriod = currentEmployeePeriod(),
    val isLoading: Boolean = false,
    val records: List<AttendanceRecord> = emptyList(),
    val errorMessage: String? = null
)
