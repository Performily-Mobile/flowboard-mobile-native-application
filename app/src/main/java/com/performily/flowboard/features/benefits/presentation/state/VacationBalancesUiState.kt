package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.VacationBalance

/** Lista de saldos de vacaciones para RR.HH., con búsqueda por nombre. */
data class VacationBalancesUiState(
    val isLoading: Boolean = false,
    val balances: List<VacationBalance> = emptyList(),
    val errorMessage: String? = null,
    val query: String = ""
) {
    val filtered: List<VacationBalance>
        get() {
            val text = query.trim().lowercase()
            if (text.isEmpty()) return balances
            return balances.filter {
                it.employeeName.orEmpty().lowercase().contains(text) || it.areaName.orEmpty().lowercase().contains(text)
            }
        }
}
