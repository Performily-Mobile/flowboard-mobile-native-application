package com.performily.flowboard.features.workspace.infrastructure.remote

data class OrganizationChartDto(
    val nodes: List<OrganizationChartNodeDto>?,
    val pendingReassignment: List<OrganizationChartNodeDto>?
)
