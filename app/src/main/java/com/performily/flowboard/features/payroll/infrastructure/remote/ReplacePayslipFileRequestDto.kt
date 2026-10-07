package com.performily.flowboard.features.payroll.infrastructure.remote

import java.math.BigDecimal

/** Cuerpo de PUT /payslips/{id}/file (ReplacePayslipFileResource). */
data class ReplacePayslipFileRequestDto(
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long,
    val storageUrl: String,
    val issueDate: String,
    val netAmount: BigDecimal,
    val currency: String
)
