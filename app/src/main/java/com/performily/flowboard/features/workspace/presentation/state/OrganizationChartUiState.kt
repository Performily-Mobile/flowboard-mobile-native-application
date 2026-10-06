package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.valueobject.OrganizationChart

enum class OrganizationChartMode {
    WHOLE_ORGANIZATION,
    BY_AREA
}

data class OrganizationChartUiState(
    val isLoading: Boolean = false,
    val mode: OrganizationChartMode = OrganizationChartMode.WHOLE_ORGANIZATION,
    val areas: List<Area> = emptyList(),
    val selectedAreaId: Long? = null,
    val chart: OrganizationChart? = null,
    val highlightedEmployeeId: EmployeeId? = null,
    val errorMessage: String? = null
)
