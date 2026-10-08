package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.SyncedVacationBalance

/** MA-57 · Saldo de vacaciones del colaborador. */
data class MyVacationBalanceUiState(
    val isLoading: Boolean = false,
    val synced: SyncedVacationBalance? = null,
    val errorMessage: String? = null
) {
    /** Se muestran datos guardados porque no hubo conexión. */
    val isOffline: Boolean get() = synced?.fromCache == true
}
