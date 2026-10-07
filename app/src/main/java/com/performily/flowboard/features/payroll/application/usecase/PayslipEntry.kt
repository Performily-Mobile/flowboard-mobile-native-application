package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.PayrollEmployee
import com.performily.flowboard.features.payroll.domain.entity.Payslip

/**
 * Boleta junto con su colaborador (de Workspace), para las listas de RR.HH.
 * employee es null si Workspace no devolvió al colaborador.
 */
data class PayslipEntry(
    val payslip: Payslip,
    val employee: PayrollEmployee?
)
