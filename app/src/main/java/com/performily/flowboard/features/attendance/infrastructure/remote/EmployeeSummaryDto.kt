package com.performily.flowboard.features.attendance.infrastructure.remote

data class EmployeeSummaryDto(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val areaId: Long?,
    val areaName: String?,
    val positionTitle: String? = null
)
