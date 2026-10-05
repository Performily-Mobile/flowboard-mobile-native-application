package com.performily.flowboard.features.workspace.domain.valueobject

data class OrganizationChart(
    val nodes: List<OrganizationChartNode>,
    val pendingReassignment: List<OrganizationChartNode>
) {
    val isEmpty: Boolean get() = nodes.isEmpty() && pendingReassignment.isEmpty()
}
