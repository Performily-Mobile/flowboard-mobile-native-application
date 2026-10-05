package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus

data class EmployeesUiState(
    val isLoading: Boolean = false,
    val employees: List<Employee> = emptyList(),
    val query: String = "",
    val areas: List<Area> = emptyList(),
    val positions: List<Position> = emptyList(),
    val selectedArea: Area? = null,
    val selectedStatus: EmploymentStatus? = EmploymentStatus.ACTIVE,
    val selectedPosition: Position? = null,
    val errorMessage: String? = null
) {
    val hasFilters: Boolean
        get() = query.isNotBlank() || selectedArea != null || selectedStatus != null || selectedPosition != null

    val positionsForFilter: List<Position>
        get() = selectedArea?.let { area -> positions.filter { it.belongsTo(area.id) } } ?: positions
}
