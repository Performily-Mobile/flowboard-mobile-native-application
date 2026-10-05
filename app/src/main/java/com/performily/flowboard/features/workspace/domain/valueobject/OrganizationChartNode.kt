package com.performily.flowboard.features.workspace.domain.valueobject

import com.performily.flowboard.core.domain.EmployeeId

data class OrganizationChartNode(
    val employeeId: EmployeeId,
    val fullName: String,
    val positionTitle: String,
    val areaName: String,
    val subordinates: List<OrganizationChartNode>
) {
    val initials: String
        get() = fullName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
