package com.performily.flowboard.features.payroll.presentation.state

import com.performily.flowboard.features.payroll.application.usecase.PayslipEntry
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import java.time.LocalDate

/** Estado de MA-69 · Estado de pagos (RR.HH.). */
data class PaymentStatusUiState(
    val periods: List<PayrollPeriod> = emptyList(),
    val selectedPeriod: PayrollPeriod? = null,
    val areas: List<PayrollArea> = emptyList(),
    val selectedArea: PayrollArea? = null,
    /** Por defecto se ven los depósitos pendientes (US46, escenario 2). */
    val selectedStatus: PaymentStatus? = PaymentStatus.PENDING,
    val isLoading: Boolean = false,
    val entries: List<PayslipEntry> = emptyList(),
    val errorMessage: String? = null,
    val sheet: PaymentSheetState? = null,
    val message: String? = null
)

/** Hoja "Actualizar estado de pago" abierta para una boleta. */
data class PaymentSheetState(
    val entry: PayslipEntry,
    val subtitle: String,
    val choice: PaymentStatus? = null,
    val paidOn: LocalDate? = null,
    val reason: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val canSave: Boolean
        get() = !isSaving && when (choice) {
            PaymentStatus.PAID -> paidOn != null
            PaymentStatus.OBSERVED -> reason.isNotBlank() && reason.trim().length <= PaymentDetails.REASON_MAX_LENGTH
            else -> false
        }
}
