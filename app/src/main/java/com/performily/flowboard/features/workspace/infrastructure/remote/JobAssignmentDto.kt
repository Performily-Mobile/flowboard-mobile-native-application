package com.performily.flowboard.features.workspace.infrastructure.remote

data class JobAssignmentDto(
    val id: Long,
    val areaId: Long,
    val areaName: String?,
    val positionId: Long,
    val positionTitle: String?,
    val changeType: String,
    val startDate: String,
    val endDate: String?,
    val current: Boolean
)
