package com.performily.flowboard.features.payroll.infrastructure.remote

/** Respuesta de PATCH /payroll-periods/{id}/publish-payslips. */
data class PublishedPayslipsDto(
    val payrollPeriodId: Long,
    val publishedCount: Int
)
