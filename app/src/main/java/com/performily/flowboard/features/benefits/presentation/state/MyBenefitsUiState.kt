package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits

/** MA-58 / MA-59 · Mis beneficios. Pestaña 0: Vigentes; 1: Entregados. */
data class MyBenefitsUiState(
    val isLoading: Boolean = false,
    val benefits: EmployeeBenefits? = null,
    val errorMessage: String? = null,
    val selectedTab: Int = 0
) {
    val currentCount: Int get() = benefits?.current?.size ?: 0
    val deliveredCount: Int get() = benefits?.delivered?.size ?: 0
}
