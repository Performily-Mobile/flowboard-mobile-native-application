package com.performily.flowboard.features.payroll.infrastructure.remote

/** Cuerpo de PATCH /payslips/{id}/mark-as-observed. */
data class MarkPayslipAsObservedRequestDto(
    val reason: String
)
