package com.performily.flowboard.features.attendance.domain.entity

import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus

data class AttendanceEmployeeSummary(
    val employeeId: Long,
    val employeeName: String,
    val effectiveHours: Double,
    val overtimeHours: Double,
    val lateCount: Int,
    val absenceCount: Int,
    val status: AttendanceStatus
)
