package com.performily.flowboard.features.attendance.domain.entity

data class AttendanceAreaReport(
    val areaId: Long,
    val areaName: String,
    val punctualityPercentage: Int,
    val lateCount: Int,
    val absenceCount: Int,
    val totalEffectiveHours: Double,
    val totalOvertimeHours: Double,
    val employees: List<AttendanceEmployeeSummary>,
    val records: List<AttendanceRecord> = emptyList()
)
