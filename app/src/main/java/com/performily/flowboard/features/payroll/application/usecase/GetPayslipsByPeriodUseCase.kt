package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import javax.inject.Inject

/**
 * Returns the payslips uploaded in a period (HR view) with the name of their employee,
 * sorted alphabetically by last name.
 *
 * If Workspace does not respond, the list is still shown without the Workspace names.
 */
class GetPayslipsByPeriodUseCase @Inject constructor(
    private val repository: PayslipRepository,
    private val employeeDirectory: PayrollEmployeeDirectory
) {

    suspend operator fun invoke(payrollPeriodId: Long): Result<List<PayslipEntry>> =
        repository.getPayslipsByPeriod(payrollPeriodId).map { payslips ->
            val employees = employeeDirectory.getEmployees().getOrDefault(emptyList()).associateBy { it.id }
            payslips
                .map { PayslipEntry(it, employees[it.employeeId]) }
                .sortedBy { (it.employee?.sortableName ?: it.payslip.employeeName.orEmpty()).lowercase() }
        }
}
