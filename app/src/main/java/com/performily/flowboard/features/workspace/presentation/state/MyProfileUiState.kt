package com.performily.flowboard.features.workspace.presentation.state

import com.performily.flowboard.features.workspace.domain.entity.Employee

data class MyProfileUiState(
    val isLoading: Boolean = false,
    val employee: Employee? = null,
    val directManager: Employee? = null,
    val errorMessage: String? = null
)
