package com.performily.flowboard.features.payroll.infrastructure.remote

import java.math.BigDecimal

/** Respuesta PayslipResource del backend (GET/POST/PUT/PATCH /payslips). */
data class PayslipDto(
    val id: Long,
    val employeeId: Long,
    val employeeName: String?,
    val payrollPeriodId: Long,
    val periodYear: Int,
    val periodMonth: Int,
    val periodLabel: String?,
    val fileName: String,
    val contentType: String?,
    val sizeInBytes: Long,
    val issueDate: String,
    val netAmount: BigDecimal,
    val currency: String?,
    val publicationStatus: String,
    val publishedAt: String?,
    val paymentStatus: String,
    val paidOn: String?,
    val observationReason: String?
)
