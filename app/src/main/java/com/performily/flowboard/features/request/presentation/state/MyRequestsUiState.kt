package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.features.request.domain.entity.Request

/**
 * State of the "Mis solicitudes" screen that HR staff open from "Más".
 */
data class MyRequestsUiState(
    val isLoading: Boolean = false,
    val requests: List<Request> = emptyList(),
    val errorMessage: String? = null,
    val filter: MyRequestsFilter = MyRequestsFilter.ALL,
    val paging: PagingState = PagingState(),
    val snackbarMessage: String? = null
)
