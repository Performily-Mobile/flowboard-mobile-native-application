package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus

/**
 * MA-70 - Offices.
 *
 * @property hasLoaded true after the first backend response, even if the list is empty
 * @property errorMessage error of the last load; when [offices] is not empty it means the data is stale
 */
data class OfficesUiState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val offices: List<OfficeStatus> = emptyList(),
    val errorMessage: String? = null
)
