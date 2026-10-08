package com.performily.flowboard.features.attendance.infrastructure.remote

data class AreaDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val active: Boolean = true,
    val activeEmployees: Long = 0
)
