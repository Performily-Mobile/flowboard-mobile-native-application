package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.Payslip

/** Resultado de cargar una boleta: o se registró, o el colaborador ya tenía una en el período. */
sealed interface PayslipUploadResult {

    data class Uploaded(val payslip: Payslip) : PayslipUploadResult

    data class Duplicate(val existing: Payslip) : PayslipUploadResult
}
