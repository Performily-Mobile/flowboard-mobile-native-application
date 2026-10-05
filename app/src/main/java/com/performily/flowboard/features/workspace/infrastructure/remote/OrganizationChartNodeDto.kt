package com.performily.flowboard.features.workspace.infrastructure.remote

data class OrganizationChartNodeDto(
    val employeeId: Long,
    val fullName: String,
    val positionTitle: String?,
    val areaName: String?,
    val subordinates: List<OrganizationChartNodeDto>?
)
