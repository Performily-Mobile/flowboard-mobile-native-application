package com.performily.flowboard.features.workspace.infrastructure.remote

data class AreaDto(
    val id: Long,
    val name: String,
    val description: String?,
    val active: Boolean,
    val activeEmployees: Long
)
