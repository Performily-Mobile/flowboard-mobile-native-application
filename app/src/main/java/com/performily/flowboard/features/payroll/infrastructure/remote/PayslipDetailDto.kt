package com.performily.flowboard.features.payroll.infrastructure.remote

/** PayslipDetailResource of the backend (GET /payslips/me/{payslipId}). */
data class PayslipDetailDto(
    val payslip: PayslipDto,
    val companyLegalName: String?,
    val companyTaxId: String?
)
