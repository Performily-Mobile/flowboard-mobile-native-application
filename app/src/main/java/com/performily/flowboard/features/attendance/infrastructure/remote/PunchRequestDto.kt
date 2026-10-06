package com.performily.flowboard.features.attendance.infrastructure.remote

data class PunchRequestDto(
    val employeeId: Long,
    val type: String,
    val punchedAt: String
)
