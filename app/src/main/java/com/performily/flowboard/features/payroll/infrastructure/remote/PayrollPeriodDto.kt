package com.performily.flowboard.features.payroll.infrastructure.remote

/** Respuesta PayrollPeriodResource del backend. */
data class PayrollPeriodDto(
    val id: Long,
    val year: Int,
    val month: Int,
    val label: String?,
    val scheduledPaymentDate: String
)
