package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.features.request.domain.entity.Request


data class ResolvedRequestsUiState(
    val isLoading: Boolean = false,
    val requests: List<Request> = emptyList(),
    val errorMessage: String? = null
)
