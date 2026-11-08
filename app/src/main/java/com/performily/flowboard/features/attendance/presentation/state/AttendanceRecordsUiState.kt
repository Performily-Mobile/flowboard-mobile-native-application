package com.performily.flowboard.features.attendance.presentation.state

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import java.time.LocalDate

private fun currentMonth(): AttendancePeriod {
    val date = LocalDate.now()
    return AttendancePeriod(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()))
}

data class AttendanceRecordsUiState(
    val period: AttendancePeriod = currentMonth(),
    val statusFilter: String = "TODOS",
    val records: List<AttendanceRecord> = emptyList(),
    val total: Long = 0,
    val nextPage: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
