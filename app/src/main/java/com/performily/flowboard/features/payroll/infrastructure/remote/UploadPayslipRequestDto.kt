package com.performily.flowboard.features.payroll.infrastructure.remote

import java.math.BigDecimal

/** Cuerpo de POST /payslips (UploadPayslipResource). */
data class UploadPayslipRequestDto(
    val employeeId: Long,
    val payrollPeriodId: Long,
    val fileName: String,
    val contentType: String,
    val sizeInBytes: Long,
    val storageUrl: String,
    val issueDate: String,
    val netAmount: BigDecimal,
    val currency: String
)
