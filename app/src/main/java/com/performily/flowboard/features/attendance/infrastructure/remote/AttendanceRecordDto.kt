package com.performily.flowboard.features.attendance.infrastructure.remote

data class AttendanceRecordDto(
    val id: Long?,
    val employeeId: Long,
    val employeeName: String?,
    val firstName: String?,
    val lastName: String?,
    val areaId: Long?,
    val areaName: String?,
    val positionTitle: String?,
    val workDate: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val effectiveHours: Double?,
    val workedHours: Double?,
    val overtimeHours: Double?,
    val overtime: Double?,
    val status: String
)
