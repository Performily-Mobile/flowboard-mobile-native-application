package com.performily.flowboard.features.payroll.presentation.state

import com.performily.flowboard.features.payroll.application.usecase.PayslipEntry
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod

/** Estado de MA-67 / MA-68 · Boletas y pagos (RR.HH.). */
data class UploadPayslipsUiState(
    val isLoadingPeriods: Boolean = false,
    val periods: List<PayrollPeriod> = emptyList(),
    val selectedPeriod: PayrollPeriod? = null,
    val isLoadingPayslips: Boolean = false,
    val entries: List<PayslipEntry> = emptyList(),
    val errorMessage: String? = null,
    /** Errores de los archivos elegidos (formato, nombre, tamaño, carga). */
    val bannerMessage: String? = null,
    val upload: UploadProgress? = null,
    val duplicate: DuplicatePrompt? = null,
    val isPublishing: Boolean = false,
    /** Mensaje de una sola vez para el snackbar. */
    val message: String? = null
) {
    val isUploading: Boolean get() = upload != null

    val underReviewCount: Int get() = entries.count { it.payslip.isUnderReview }

    val canPublish: Boolean
        get() = selectedPeriod != null && underReviewCount > 0 && !isPublishing && !isUploading
}

/** Avance de la carga en curso: archivo [current] de [total]. */
data class UploadProgress(val current: Int, val total: Int)

/** MA-68: el colaborador ya tiene una boleta en el período. */
data class DuplicatePrompt(
    val employeeName: String,
    val periodLabel: String,
    val fileName: String
)
