package com.performily.flowboard.features.payroll.presentation.state

import com.performily.flowboard.features.payroll.domain.entity.PayslipDetail
import com.performily.flowboard.features.payroll.domain.valueobject.PayslipDownloadLink

/**
 * State of the payslip viewer of the employee.
 *
 * @property detail payslip with the employer data, or null until it loads.
 * @property isLoading true while the payslip loads.
 * @property errorMessage reason the payslip could not be loaded, or null.
 * @property isOpeningFile true while the download link is requested.
 * @property linkToOpen link the screen has to open once, or null.
 * @property message one-time message for a snackbar, or null.
 */
data class PayslipViewerUiState(
    val detail: PayslipDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOpeningFile: Boolean = false,
    val linkToOpen: PayslipDownloadLink? = null,
    val message: String? = null
)
