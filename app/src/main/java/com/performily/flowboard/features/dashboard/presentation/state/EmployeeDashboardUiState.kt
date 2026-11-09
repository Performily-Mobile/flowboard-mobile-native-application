package com.performily.flowboard.features.dashboard.presentation.state

import com.performily.flowboard.features.dashboard.domain.entity.EmployeeDashboard

/**
 * State of the employee dashboard screen.
 *
 * @property dashboard last loaded dashboard, kept while a refresh is in progress.
 * @property isLoading true during the first load, when there is nothing to show yet.
 * @property isRefreshing true while a pull-to-refresh or retry is in progress.
 * @property errorMessage message shown when no section could be loaded.
 */
data class EmployeeDashboardUiState(
    val dashboard: EmployeeDashboard? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
