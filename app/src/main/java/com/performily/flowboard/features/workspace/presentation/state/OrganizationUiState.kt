package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Position

data class AreaForm(
    val name: String = "",
    val description: String = "",
    val nameError: String? = null
)

data class PositionForm(
    val areaId: Long? = null,
    val title: String = "",
    val referenceSalary: String = "",
    val areaError: String? = null,
    val titleError: String? = null,
    val salaryError: String? = null
)

data class OrganizationUiState(
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val areas: List<Area> = emptyList(),
    val positions: List<Position> = emptyList(),
    val errorMessage: String? = null,
    val isAreaSheetVisible: Boolean = false,
    val isPositionSheetVisible: Boolean = false,
    val areaForm: AreaForm = AreaForm(),
    val positionForm: PositionForm = PositionForm(),
    val isSaving: Boolean = false,
    val areaToDeactivate: Area? = null,
    val deactivationConfirmed: Boolean = false,
    val deactivationError: String? = null
) {
    val activeAreas: List<Area> get() = areas.filter { it.active }
}
