package com.performily.flowboard.features.attendance.domain.entity

data class AttendanceHoursReport(
    val totalEffectiveHours: Double,
    val totalOvertimeHours: Double,
    val employees: List<EmployeeHoursSummary>
)

data class EmployeeHoursSummary(
    val employeeId: Long,
    val employeeName: String,
    val effectiveHours: Double,
    val overtimeHours: Double
)
