package com.performily.flowboard.features.attendance.domain.entity

import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import java.time.LocalDate
import java.time.LocalTime

data class AttendanceRecord(
    val id: Long?,
    val employeeId: Long,
    val employeeName: String?,
    val areaId: Long?,
    val areaName: String?,
    val positionTitle: String?,
    val workDate: LocalDate,
    val checkInTime: LocalTime?,
    val checkOutTime: LocalTime?,
    val effectiveHours: Double?,
    val overtimeHours: Double?,
    val status: AttendanceStatus
){
    val isAbsent: Boolean get() = status == AttendanceStatus.ABSENT
    val isJustified: Boolean get() = status == AttendanceStatus.JUSTIFIED
    val isComplete: Boolean get() = checkInTime != null && checkOutTime != null
}
