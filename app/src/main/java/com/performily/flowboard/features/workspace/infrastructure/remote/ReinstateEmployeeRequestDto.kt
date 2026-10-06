package com.performily.flowboard.features.workspace.infrastructure.remote

data class ReinstateEmployeeRequestDto(
    val areaId: Long,
    val positionId: Long,
    val reinstatementDate: String
)
