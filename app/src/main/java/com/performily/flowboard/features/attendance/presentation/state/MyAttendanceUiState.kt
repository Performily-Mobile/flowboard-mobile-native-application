package com.performily.flowboard.features.attendance.presentation.state

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import java.time.LocalDate

private fun currentMonthPeriod(): AttendancePeriod {
    val today = LocalDate.now()
    return AttendancePeriod(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()))
}

data class MyAttendanceUiState(
    val period: AttendancePeriod = currentMonthPeriod(),
    val statusFilter: String = "TODOS",
    val isLoading: Boolean = false,
    val isPunching: Boolean = false,
    val records: List<AttendanceRecord> = emptyList(),
    val errorMessage: String? = null,
    val actionMessage: String? = null
) {
    val filteredRecords: List<AttendanceRecord>
        get() = if (statusFilter == "TODOS") records else records.filter { it.status.name == statusFilter }

    val todayRecord: AttendanceRecord?
        get() = records.firstOrNull { it.workDate == LocalDate.now() }
}
