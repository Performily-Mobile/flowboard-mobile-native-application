package com.performily.flowboard.features.payroll.presentation.state

import com.performily.flowboard.features.payroll.domain.entity.Payslip

/**
 * State of the "Boletas" tab of the employee.
 *
 * @property years years that can be selected, the current one first.
 * @property selectedYear year whose payslips are shown.
 * @property payslips published payslips of [selectedYear].
 * @property isLoading true while the first load of a year runs.
 * @property isRefreshing true while a pull-to-refresh runs.
 * @property errorMessage reason of the last failure, or null.
 */
data class MyPayslipsUiState(
    val years: List<Int>,
    val selectedYear: Int,
    val payslips: List<Payslip> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
