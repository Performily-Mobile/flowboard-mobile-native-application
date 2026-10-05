package com.performily.flowboard.features.workspace.infrastructure.mapper

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChart
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChartNode
import com.performily.flowboard.features.workspace.infrastructure.remote.OrganizationChartDto
import com.performily.flowboard.features.workspace.infrastructure.remote.OrganizationChartNodeDto

object OrganizationChartMapper {

    fun toDomain(dto: OrganizationChartDto): OrganizationChart {
        return OrganizationChart(
            nodes = dto.nodes.orEmpty().map(::toNode),
            pendingReassignment = dto.pendingReassignment.orEmpty().map(::toNode)
        )
    }

    private fun toNode(dto: OrganizationChartNodeDto): OrganizationChartNode {
        return OrganizationChartNode(
            employeeId = EmployeeId(dto.employeeId),
            fullName = dto.fullName,
            positionTitle = dto.positionTitle.orEmpty(),
            areaName = dto.areaName.orEmpty(),
            subordinates = dto.subordinates.orEmpty().map(::toNode)
        )
    }
}
