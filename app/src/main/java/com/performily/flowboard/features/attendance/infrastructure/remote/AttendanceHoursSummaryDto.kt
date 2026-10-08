package com.performily.flowboard.features.attendance.infrastructure.remote

data class AttendanceHoursSummaryDto(
    val employeeId: Long,
    val workedHours: Double,
    val overtimeHours: Double
)
