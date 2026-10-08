package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.VacationBalance
import com.performily.flowboard.features.benefits.domain.valueobject.AdjustmentOperation

/** Hoja "Ajustar saldo" (MA-63). */
data class AdjustmentForm(
    val operation: AdjustmentOperation = AdjustmentOperation.ADD,
    val days: String = "",
    val reason: String = "",
    val daysError: String? = null,
    val reasonError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

/** MA-63 · Saldo de vacaciones de un colaborador (RR.HH.). */
data class VacationBalanceDetailUiState(
    val employeeId: Long? = null,
    val isLoading: Boolean = false,
    val balance: VacationBalance? = null,
    val errorMessage: String? = null,
    val isAdjustSheetVisible: Boolean = false,
    val adjustmentForm: AdjustmentForm = AdjustmentForm(),
    val snackbarMessage: String? = null
)
