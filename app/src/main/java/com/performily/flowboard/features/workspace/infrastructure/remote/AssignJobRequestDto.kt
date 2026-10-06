package com.performily.flowboard.features.workspace.infrastructure.remote

data class AssignJobRequestDto(
    val areaId: Long,
    val positionId: Long,
    val effectiveDate: String
)
