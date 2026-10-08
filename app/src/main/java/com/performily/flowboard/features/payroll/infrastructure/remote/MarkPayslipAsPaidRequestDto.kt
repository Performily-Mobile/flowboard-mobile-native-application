package com.performily.flowboard.features.payroll.infrastructure.remote

/** Cuerpo de PATCH /payslips/{id}/mark-as-paid. */
data class MarkPayslipAsPaidRequestDto(
    val paidOn: String
)
