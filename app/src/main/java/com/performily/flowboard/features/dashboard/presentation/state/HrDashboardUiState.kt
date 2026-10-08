package com.performily.flowboard.features.dashboard.presentation.state

import com.performily.flowboard.features.dashboard.domain.entity.HrDashboard

/**
 * State of the HR dashboard screen.
 *
 * @property dashboard last loaded dashboard, kept while a refresh is in progress.
 * @property isLoading true during the first load, when there is nothing to show yet.
 * @property isRefreshing true while a pull-to-refresh or retry is in progress.
 * @property errorMessage message shown when no section could be loaded.
 */
data class HrDashboardUiState(
    val dashboard: HrDashboard? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
