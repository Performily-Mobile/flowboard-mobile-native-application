package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.PayrollEmployee
import com.performily.flowboard.features.payroll.domain.entity.Payslip

/**
 * A payslip together with its employee (from Workspace), for the HR lists.
 *
 * @property payslip the payslip
 * @property employee the employee, or null when Workspace did not return it
 */
data class PayslipEntry(
    val payslip: Payslip,
    val employee: PayrollEmployee?
) {
    /**
     * Full name of the employee, for example "Pedro Huamán Quispe".
     *
     * Falls back to the name sent with the payslip and then to "Colaborador 12" when Workspace
     * did not return the employee.
     */
    val fullName: String
        get() = employee?.fullName?.takeIf { it.isNotBlank() }
            ?: payslip.employeeName?.trim()?.takeIf { it.isNotEmpty() }
            ?: "Colaborador ${payslip.employeeId.value}"
}
