package com.performily.flowboard.features.attendance.infrastructure.remote

data class JustifyAttendanceRequestDto(
    val reason: String,
    val evidenceUrl: String?
)
