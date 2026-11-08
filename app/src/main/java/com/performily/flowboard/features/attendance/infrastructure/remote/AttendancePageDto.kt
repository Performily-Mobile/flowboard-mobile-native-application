package com.performily.flowboard.features.attendance.infrastructure.remote

data class AttendancePageDto(
    val content: List<AttendanceRecordDto>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val last: Boolean
)
