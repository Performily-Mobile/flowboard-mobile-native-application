package com.performily.flowboard.features.attendance.domain.entity

data class AttendanceEmployee(
    val id: Long,
    val fullName: String,
    val areaId: Long?,
    val areaName: String?
)
