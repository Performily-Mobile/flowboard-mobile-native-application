package com.performily.flowboard.features.attendance.infrastructure.remote

data class AttendanceAreaSummaryDto(
    val areaId: Long,
    val onTime: Long,
    val late: Long,
    val absent: Long,
    val incomplete: Long,
    val justified: Long,
    val workedHours: Double,
    val overtimeHours: Double
)
