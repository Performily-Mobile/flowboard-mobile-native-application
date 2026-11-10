package com.performily.flowboard.features.payroll.domain.entity

/**
 * Payslip together with the employer data printed on it, shown in the payslip viewer.
 *
 * @property payslip payslip of the employee.
 * @property companyLegalName legal name of the employer, or null when the backend did not send it.
 * @property companyTaxId tax identifier (RUC) of the employer, or null when the backend did not send it.
 */
data class PayslipDetail(
    val payslip: Payslip,
    val companyLegalName: String?,
    val companyTaxId: String?
)
