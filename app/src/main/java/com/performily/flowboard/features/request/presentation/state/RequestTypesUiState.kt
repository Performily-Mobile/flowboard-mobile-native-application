package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.features.request.domain.entity.RequestType


data class RequestTypesSnackbar(
    val message: String,
    val typeToDeactivate: RequestType? = null
)

data class RequestTypesUiState(
    val isLoading: Boolean = false,
    val types: List<RequestType> = emptyList(),
    val errorMessage: String? = null,
    val typeToDelete: RequestType? = null,
    val isWorking: Boolean = false,
    val snackbar: RequestTypesSnackbar? = null
)
